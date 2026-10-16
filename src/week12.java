// ER Triage System - Week 12: streams for the shift report
// Compile and run: javac week12.java && java Main

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.stream.Collectors;

// ===== Main.java =====
// Week 10: the shift is loaded from ./data at startup and saved after every action.
class Main {
    static final Scanner in = new Scanner(System.in);
    static TriageUnit unit;

    public static void main(String[] args) throws IOException {
        Storage storage = new Storage(Path.of("data"));
        unit = storage.load(new RuleBasedScorer());

        System.out.println("=== ER Triage === (cases on file: " + unit.allCases().size() + ")");
        boolean running = true;
        while (running) {
            System.out.println("1) Admit  2) Call next  3) Discharge  4) Re-triage  5) Report  6) Exit");
            System.out.print("Choice: ");
            String choice = in.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> admit();
                    case "2" -> callNext();
                    case "3" -> discharge();
                    case "4" -> retriage();
                    case "5" -> System.out.print(ShiftReport.render(unit));
                    case "6" -> running = false;
                    default -> System.out.println("Please pick 1-6.");
                }
            } catch (InvalidTransitionException | NoDoctorAvailableException | IllegalArgumentException e) {
                System.out.println("! " + e.getMessage());
            }
            storage.save(unit);
        }
        System.out.println("Shift saved. Goodbye.");
    }

    static void admit() {
        System.out.print("Name: ");
        String name = in.nextLine().trim();
        System.out.print("Age: ");
        int age = Integer.parseInt(in.nextLine().trim());
        Vitals vitals = readVitals();
        Case c = unit.admit(name, age, vitals);
        System.out.printf("-> case %d | %s | severity %d (%s)%n",
                c.getCaseId(), vitals, c.getSeverity(), Severity.label(c.getSeverity()));
    }

    static void callNext() {
        Case c = unit.callNext();
        if (c == null) System.out.println("No patients waiting.");
        else System.out.printf("-> Calling %s (sev %d) to %s%n",
                c.getPatient().getName(), c.getSeverity(), c.getDoctor().getName());
    }

    static void discharge() {
        int id = askCaseId();
        unit.discharge(id);
        System.out.println("-> Case " + id + " discharged.");
    }

    static void retriage() {
        int id = askCaseId();
        unit.retriage(id, readVitals());
        Case c = unit.find(id);
        System.out.printf("-> Case %d now severity %d (%s)%n", id, c.getSeverity(), Severity.label(c.getSeverity()));
    }

    static int askCaseId() {
        System.out.print("Case id: ");
        return Integer.parseInt(in.nextLine().trim());
    }

    static Vitals readVitals() {
        System.out.print("Vitals (SYS/DIA HR TEMP, e.g. 90/60 130 36.8): ");
        return Vitals.parse(in.nextLine());
    }
}

// ===== AuditLog.java =====
// Append-only audit trail. Every state change is stamped with the time it happened and saved with the shift.
final class AuditLog {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final List<String> lines = new ArrayList<>();

    void record(int caseId, String event) {
        lines.add(LocalDateTime.now().format(FMT) + " | case " + caseId + " | " + event);
    }

    void restore(List<String> savedLines) {
        lines.addAll(savedLines);
    }

    List<String> lines() {
        return List.copyOf(lines);
    }
}

// ===== Case.java =====
// One visit to the ER. Status changes only through the methods below, and each one checks the transition first.
final class Case implements Comparable<Case> {
    // Urgency order: most severe first, then earliest arrival, then case id as a final tie-break.
    static final Comparator<Case> URGENCY = Comparator
            .comparingInt(Case::getSeverity)
            .thenComparingLong(Case::getArrivalMillis)
            .thenComparingInt(Case::getCaseId);

    private final int caseId;
    private final Patient patient;
    private int severity;
    private final long arrivalMillis;
    private long calledAtMillis = -1;
    private Status status = Status.WAITING;
    private Doctor doctor;

    Case(int caseId, Patient patient, int severity, long arrivalMillis) {
        this.caseId = caseId;
        this.patient = patient;
        this.severity = severity;
        this.arrivalMillis = arrivalMillis;
    }

    // Used when reloading a saved case from disk.
    Case(int caseId, Patient patient, int severity, long arrivalMillis,
         long calledAtMillis, Status status, Doctor doctor) {
        this(caseId, patient, severity, arrivalMillis);
        this.calledAtMillis = calledAtMillis;
        this.status = status;
        this.doctor = doctor;
    }

    void startTreatment(Doctor doctor, long now) {
        if (status != Status.WAITING) {
            throw new InvalidTransitionException("cannot call case " + caseId + " while it is " + status);
        }
        this.status = Status.IN_TREATMENT;
        this.doctor = doctor;
        this.calledAtMillis = now;
    }

    void discharge() {
        if (status != Status.IN_TREATMENT) {
            throw new InvalidTransitionException("cannot discharge case " + caseId + " while it is " + status);
        }
        this.status = Status.DISCHARGED;
    }

    void reassess(Vitals vitals, int newSeverity) {
        if (status != Status.WAITING) {
            throw new InvalidTransitionException("only waiting patients can be re-triaged; case " + caseId + " is " + status);
        }
        patient.updateVitals(vitals);
        this.severity = newSeverity;
    }

    boolean hasBeenCalled() { return calledAtMillis >= 0; }

    double waitMinutes() { return (calledAtMillis - arrivalMillis) / 60_000.0; }

    int getCaseId() { return caseId; }
    Patient getPatient() { return patient; }
    int getSeverity() { return severity; }
    long getArrivalMillis() { return arrivalMillis; }
    long getCalledAtMillis() { return calledAtMillis; }
    Status getStatus() { return status; }
    Doctor getDoctor() { return doctor; }

    @Override
    public int compareTo(Case other) {
        return URGENCY.compare(this, other);
    }
}

// ===== Doctor.java =====
// A doctor in the pool. A doctor treats at most one case at a time.
final class Doctor {
    private final String id;
    private final String name;
    private boolean busy;
    private Case currentCase;

    Doctor(String id, String name) {
        this.id = id;
        this.name = name;
    }

    String getId() { return id; }
    String getName() { return name; }
    boolean isBusy() { return busy; }
    Case getCurrentCase() { return currentCase; }

    void assign(Case c) {
        if (busy) throw new IllegalStateException(name + " is already busy");
        busy = true;
        currentCase = c;
    }

    void release() {
        busy = false;
        currentCase = null;
    }

    String describe() {
        return busy ? name + " (busy with case " + currentCase.getCaseId() + ")" : name + " (free)";
    }
}

// ===== InvalidTransitionException.java =====
// Thrown when a case is asked to move to a status it cannot reach from where it is.
class InvalidTransitionException extends IllegalStateException {
    InvalidTransitionException(String message) {
        super(message);
    }
}

// ===== InvalidVitalsException.java =====
// Thrown when vitals are missing, malformed or physiologically impossible.
class InvalidVitalsException extends IllegalArgumentException {
    InvalidVitalsException(String message) {
        super(message);
    }
}

// ===== NoDoctorAvailableException.java =====
// Thrown when patients are waiting but every doctor is busy.
class NoDoctorAvailableException extends IllegalStateException {
    NoDoctorAvailableException(String message) {
        super(message);
    }
}

// ===== Patient.java =====
// A person in the department. Vitals change only through updateVitals (re-triage).
final class Patient {
    private final int id;
    private final String name;
    private final int age;
    private Vitals vitals;

    Patient(int id, String name, int age, Vitals vitals) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name is required");
        if (age < 0 || age > 130) throw new IllegalArgumentException("age must be between 0 and 130");
        this.id = id;
        this.name = name.trim();
        this.age = age;
        this.vitals = vitals;
    }

    int getId() { return id; }
    String getName() { return name; }
    int getAge() { return age; }
    Vitals getVitals() { return vitals; }
    void updateVitals(Vitals newVitals) { this.vitals = newVitals; }
}

// ===== RuleBasedScorer.java =====
// Rule-based rubric: the first matching rule decides the band.
final class RuleBasedScorer implements SeverityScorer {
    @Override
    public int score(Vitals v) {
        if (v.systolic() <= 90 || v.heartRate() >= 130 || v.heartRate() <= 40 || v.temperature() >= 40.0) {
            return Severity.CRITICAL;
        }
        if (v.systolic() < 110 || v.heartRate() >= 110 || v.temperature() >= 39.0) return Severity.URGENT;
        if (v.heartRate() >= 100 || v.temperature() >= 38.0) return Severity.SEMI_URGENT;
        return Severity.MINOR;
    }
}

// ===== Severity.java =====
// Severity bands. 1 is the most urgent.
final class Severity {
    static final int CRITICAL = 1, URGENT = 2, SEMI_URGENT = 3, MINOR = 4;

    private Severity() {}

    static String label(int level) {
        return switch (level) {
            case CRITICAL -> "CRITICAL";
            case URGENT -> "URGENT";
            case SEMI_URGENT -> "SEMI-URGENT";
            case MINOR -> "MINOR";
            default -> throw new IllegalArgumentException("unknown severity " + level);
        };
    }
}

// ===== SeverityScorer.java =====
// Maps a patient's vitals to a severity band. Swap the implementation to change the rubric without touching the queue.
interface SeverityScorer {
    int score(Vitals vitals);
}

// ===== ShiftReport.java =====
// Week 12: the shift report is computed with streams: filter by status, group by severity, average the waits.
final class ShiftReport {
    private ShiftReport() {}

    static String render(TriageUnit unit) {
        List<Case> all = unit.allCases();

        Map<Integer, Double> avgWait = all.stream()
                .filter(Case::hasBeenCalled)
                .collect(Collectors.groupingBy(Case::getSeverity, TreeMap::new,
                        Collectors.averagingDouble(Case::waitMinutes)));
        Map<Integer, Long> admittedByBand = all.stream()
                .collect(Collectors.groupingBy(Case::getSeverity, TreeMap::new, Collectors.counting()));

        List<Case> queue = unit.waitingInUrgencyOrder();
        long treating = count(all, Status.IN_TREATMENT);
        long discharged = count(all, Status.DISCHARGED);

        String waits = avgWait.isEmpty() ? "no patients called yet" : avgWait.entrySet().stream()
                .map(e -> String.format("sev%d %.0fm", e.getKey(), e.getValue()))
                .collect(Collectors.joining(" | "));
        String bands = admittedByBand.entrySet().stream()
                .map(e -> Severity.label(e.getKey()) + " " + e.getValue())
                .collect(Collectors.joining(", "));

        StringBuilder sb = new StringBuilder("\n--- Shift report ---\n");
        sb.append("Avg wait: ").append(waits).append('\n');
        sb.append("Admitted by band: ").append(bands).append('\n');
        sb.append("In queue: ").append(queue.size())
                .append(" | In treatment: ").append(treating)
                .append(" | Discharged: ").append(discharged).append('\n');
        sb.append("Queue (most urgent first):\n");
        if (queue.isEmpty()) sb.append("  (empty)\n");
        long now = System.currentTimeMillis();
        for (Case c : queue) {
            double waited = (now - c.getArrivalMillis()) / 60_000.0;
            sb.append(String.format("  #%d %-15s %-12s waiting %.0f min%n", c.getCaseId(),
                    c.getPatient().getName(), Severity.label(c.getSeverity()), waited));
        }
        sb.append("Doctors:\n");
        unit.doctorPool().forEach(d -> sb.append("  ").append(d.describe()).append('\n'));
        return sb.toString();
    }

    private static long count(List<Case> cases, Status status) {
        return cases.stream().filter(c -> c.getStatus() == status).count();
    }
}

// ===== Status.java =====
// The lifecycle of a case. A case only ever moves WAITING -> IN_TREATMENT -> DISCHARGED.
enum Status { WAITING, IN_TREATMENT, DISCHARGED }

// ===== Storage.java =====
// Saves and reloads a shift as plain text files in one folder, so the shift can resume after a restart.
final class Storage {
    private final Path dir;

    Storage(Path dir) {
        this.dir = dir;
    }

    void save(TriageUnit unit) throws IOException {
        Files.createDirectories(dir);

        List<String> doctorLines = new ArrayList<>();
        for (Doctor d : unit.doctorPool()) {
            doctorLines.add(String.join(",", d.getId(), clean(d.getName())));
        }

        List<String> patientLines = new ArrayList<>();
        List<String> caseLines = new ArrayList<>();
        for (Case c : unit.allCases()) {
            Patient p = c.getPatient();
            Vitals v = p.getVitals();
            patientLines.add(String.join(",",
                    s(p.getId()), clean(p.getName()), s(p.getAge()),
                    s(v.systolic()), s(v.diastolic()), s(v.heartRate()), s(v.temperature())));
            caseLines.add(String.join(",",
                    s(c.getCaseId()), s(p.getId()), s(c.getSeverity()), s(c.getArrivalMillis()),
                    s(c.getCalledAtMillis()), c.getStatus().name(),
                    c.getDoctor() == null ? "-" : c.getDoctor().getId()));
        }

        Files.write(dir.resolve("doctors.csv"), doctorLines);
        Files.write(dir.resolve("patients.csv"), patientLines);
        Files.write(dir.resolve("cases.csv"), caseLines);
        Files.write(dir.resolve("audit.log"), unit.audit().lines());
    }

    TriageUnit load(SeverityScorer scorer) throws IOException {
        if (!Files.exists(dir.resolve("cases.csv"))) {     // first shift: start with the default doctor pool
            List<Doctor> pool = new ArrayList<>();
            pool.add(new Doctor("D1", "Dr. Menon"));
            pool.add(new Doctor("D2", "Dr. Kapoor"));
            return new TriageUnit(scorer, pool);
        }

        List<Doctor> pool = new ArrayList<>();
        for (String line : Files.readAllLines(dir.resolve("doctors.csv"))) {
            String[] f = line.split(",", -1);
            pool.add(new Doctor(f[0], f[1]));
        }
        TriageUnit unit = new TriageUnit(scorer, pool);

        Map<Integer, Patient> patients = new HashMap<>();
        for (String line : Files.readAllLines(dir.resolve("patients.csv"))) {
            String[] f = line.split(",", -1);
            int id = Integer.parseInt(f[0]);
            Vitals v = new Vitals(Double.parseDouble(f[3]), Double.parseDouble(f[4]),
                                  Double.parseDouble(f[5]), Double.parseDouble(f[6]));
            patients.put(id, new Patient(id, f[1], Integer.parseInt(f[2]), v));
        }

        for (String line : Files.readAllLines(dir.resolve("cases.csv"))) {
            String[] f = line.split(",", -1);
            Doctor doctor = f[6].equals("-") ? null : unit.doctor(f[6]);
            unit.restoreCase(new Case(Integer.parseInt(f[0]), patients.get(Integer.parseInt(f[1])),
                    Integer.parseInt(f[2]), Long.parseLong(f[3]), Long.parseLong(f[4]),
                    Status.valueOf(f[5]), doctor));
        }

        Path auditFile = dir.resolve("audit.log");
        if (Files.exists(auditFile)) unit.audit().restore(Files.readAllLines(auditFile));
        return unit;
    }

    private static String s(Object value) {
        return String.valueOf(value);
    }

    private static String clean(String text) {
        return text.replace(',', ' ');
    }
}

// ===== TriageUnit.java =====
// The department: a priority queue of waiting cases, a map of doctors, and the rules for moving patients through.
class TriageUnit {
    private final SeverityScorer scorer;
    private final PriorityQueue<Case> waiting = new PriorityQueue<>(Case.URGENCY);
    private final Map<String, Doctor> doctors = new LinkedHashMap<>();
    private final List<Case> cases = new ArrayList<>();
    private final AuditLog audit = new AuditLog();

    TriageUnit(SeverityScorer scorer, List<Doctor> pool) {
        this.scorer = scorer;
        for (Doctor d : pool) doctors.put(d.getId(), d);
    }

    Case admit(String name, int age, Vitals vitals) {
        int id = cases.size() + 1;
        int severity = scorer.score(vitals);
        Case c = new Case(id, new Patient(id, name, age, vitals), severity, System.currentTimeMillis());
        cases.add(c);
        waiting.add(c);
        audit.record(id, "ADMITTED severity " + severity);
        return c;
    }

    // Returns null if nobody waits. Throws if patients wait but every doctor is busy (the patient stays queued).
    Case callNext() {
        if (waiting.isEmpty()) return null;
        Doctor doctor = freeDoctor();
        Case next = waiting.poll();                     // head of the priority queue = most urgent
        next.startTreatment(doctor, System.currentTimeMillis());
        doctor.assign(next);
        audit.record(next.getCaseId(), "IN_TREATMENT with " + doctor.getName());
        return next;
    }

    private Doctor freeDoctor() {
        return doctors.values().stream()
                .filter(d -> !d.isBusy())
                .findFirst()
                .orElseThrow(() -> new NoDoctorAvailableException("all doctors are busy; patient stays in the queue"));
    }

    void discharge(int caseId) {
        Case c = find(caseId);
        c.discharge();
        c.getDoctor().release();
        audit.record(caseId, "DISCHARGED");
    }

    void retriage(int caseId, Vitals vitals) {
        Case c = find(caseId);
        if (c.getStatus() != Status.WAITING) {
            throw new InvalidTransitionException("only waiting patients can be re-triaged; case " + caseId + " is " + c.getStatus());
        }
        int old = c.getSeverity();
        waiting.remove(c);                              // leave the queue before its ordering key changes
        c.reassess(vitals, scorer.score(vitals));
        waiting.add(c);
        audit.record(caseId, "RE-TRIAGED severity " + old + " -> " + c.getSeverity());
    }

    Case find(int caseId) {
        return cases.stream()
                .filter(c -> c.getCaseId() == caseId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("no case with id " + caseId));
    }

    // Used by Storage when reloading a saved shift.
    void restoreCase(Case c) {
        cases.add(c);
        if (c.getStatus() == Status.WAITING) waiting.add(c);
        if (c.getStatus() == Status.IN_TREATMENT) c.getDoctor().assign(c);
    }

    Doctor doctor(String id) { return doctors.get(id); }

    // The queue, most urgent first, without disturbing the heap.
    List<Case> waitingInUrgencyOrder() {
        return waiting.stream().sorted(Case.URGENCY).toList();
    }

    List<Case> allCases() { return cases; }
    Collection<Doctor> doctorPool() { return doctors.values(); }
    AuditLog audit() { return audit; }
}

// ===== Vitals.java =====
// Vitals as an immutable record. Impossible readings are rejected when the record is built.
record Vitals(double systolic, double diastolic, double heartRate, double temperature) {

    Vitals {
        if (systolic < 50 || systolic > 260) {
            throw new InvalidVitalsException("systolic out of range: " + systolic);
        }
        if (diastolic < 30 || diastolic >= systolic) {
            throw new InvalidVitalsException("diastolic must be at least 30 and below systolic");
        }
        if (heartRate < 20 || heartRate > 250) {
            throw new InvalidVitalsException("heart rate out of range: " + heartRate);
        }
        if (temperature < 25 || temperature > 45) {
            throw new InvalidVitalsException("temperature out of range: " + temperature);
        }
    }

    // Parses "SYS/DIA HR TEMP", for example "90/60 130 36.8".
    static Vitals parse(String text) {
        String[] parts = text.trim().split("\\s+");
        if (parts.length != 3 || !parts[0].contains("/")) {
            throw new InvalidVitalsException("expected SYS/DIA HR TEMP, e.g. 90/60 130 36.8");
        }
        String[] bp = parts[0].split("/");
        if (bp.length != 2) throw new InvalidVitalsException("blood pressure must look like 90/60");
        try {
            return new Vitals(Double.parseDouble(bp[0]), Double.parseDouble(bp[1]),
                              Double.parseDouble(parts[1]), Double.parseDouble(parts[2]));
        } catch (NumberFormatException e) {
            throw new InvalidVitalsException("vitals must be numbers");
        }
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "BP %.0f/%.0f HR %.0f T %.1f", systolic, diastolic, heartRate, temperature);
    }
}
