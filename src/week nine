// ER Triage System - Week 9: records, parsing and shift report
// Compile and run: javac week09.java && java Main

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

// ===== Main.java =====
// Week 9: vitals are a Vitals record parsed from "SYS/DIA HR TEMP". The shift report is formatted text.
class Main {
    static final Scanner in = new Scanner(System.in);
    static final TriageUnit unit = new TriageUnit(new RuleBasedScorer());

    public static void main(String[] args) {
        System.out.println("=== ER Triage ===");
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
        }
        System.out.println("Shift closed.");
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
// Append-only audit trail. Every state change is stamped with the time it happened.
final class AuditLog {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final List<String> lines = new ArrayList<>();

    void record(int caseId, String event) {
        lines.add(LocalDateTime.now().format(FMT) + " | case " + caseId + " | " + event);
    }

    List<String> lines() {
        return List.copyOf(lines);
    }
}

// ===== Case.java =====
// One visit to the ER. Status changes only through the methods below, and each one checks the transition first.
final class Case implements Comparable<Case> {
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

    // Natural order: more severe first; equal severity goes to the earlier arrival.
    @Override
    public int compareTo(Case other) {
        int bySeverity = Integer.compare(severity, other.severity);
        if (bySeverity != 0) return bySeverity;
        int byArrival = Long.compare(arrivalMillis, other.arrivalMillis);
        return byArrival != 0 ? byArrival : Integer.compare(caseId, other.caseId);
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
// Week 9: formats the end-of-shift report as text. Averages are per severity band; the queue is most urgent first.
final class ShiftReport {
    private ShiftReport() {}

    static String render(TriageUnit unit) {
        double[] waitSum = new double[5];
        int[] calledCount = new int[5];
        List<Case> queue = new ArrayList<>();
        int treating = 0;
        int discharged = 0;

        for (Case c : unit.allCases()) {
            if (c.hasBeenCalled()) {
                waitSum[c.getSeverity()] += c.waitMinutes();
                calledCount[c.getSeverity()]++;
            }
            switch (c.getStatus()) {
                case WAITING -> queue.add(c);
                case IN_TREATMENT -> treating++;
                case DISCHARGED -> discharged++;
            }
        }
        Collections.sort(queue);

        List<String> waits = new ArrayList<>();
        for (int b = 1; b <= 4; b++) {
            if (calledCount[b] > 0) waits.add(String.format("sev%d %.0fm", b, waitSum[b] / calledCount[b]));
        }

        StringBuilder sb = new StringBuilder("\n--- Shift report ---\n");
        sb.append("Avg wait: ").append(waits.isEmpty() ? "no patients called yet" : String.join(" | ", waits)).append('\n');
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
        for (Doctor d : unit.doctorPool()) sb.append("  ").append(d.describe()).append('\n');
        return sb.toString();
    }
}

// ===== Status.java =====
// The lifecycle of a case. A case only ever moves WAITING -> IN_TREATMENT -> DISCHARGED.
enum Status { WAITING, IN_TREATMENT, DISCHARGED }

// ===== TriageUnit.java =====
// The department: every case, the doctor pool, and the rules for moving patients through it.
class TriageUnit {
    private final SeverityScorer scorer;
    private final List<Case> cases = new ArrayList<>();      // arrival order
    private final List<Doctor> doctors = new ArrayList<>();
    private final AuditLog audit = new AuditLog();

    TriageUnit(SeverityScorer scorer) {
        this.scorer = scorer;
        doctors.add(new Doctor("D1", "Dr. Menon"));
        doctors.add(new Doctor("D2", "Dr. Kapoor"));
    }

    Case admit(String name, int age, Vitals vitals) {
        int id = cases.size() + 1;
        int severity = scorer.score(vitals);
        Case c = new Case(id, new Patient(id, name, age, vitals), severity, System.currentTimeMillis());
        cases.add(c);
        audit.record(id, "ADMITTED severity " + severity);
        return c;
    }

    // Returns null when nobody is waiting. Throws if patients wait but no doctor is free.
    Case callNext() {
        List<Case> waiting = new ArrayList<>();
        for (Case c : cases) {
            if (c.getStatus() == Status.WAITING) waiting.add(c);
        }
        if (waiting.isEmpty()) return null;

        Doctor doctor = freeDoctor();                  // checked first, so the patient stays queued if none is free
        Case next = Collections.min(waiting);          // Comparable: severity, then arrival
        next.startTreatment(doctor, System.currentTimeMillis());
        doctor.assign(next);
        audit.record(next.getCaseId(), "IN_TREATMENT with " + doctor.getName());
        return next;
    }

    private Doctor freeDoctor() {
        for (Doctor d : doctors) {
            if (!d.isBusy()) return d;
        }
        throw new NoDoctorAvailableException("all doctors are busy; patient stays in the queue");
    }

    void discharge(int caseId) {
        Case c = find(caseId);
        c.discharge();
        c.getDoctor().release();
        audit.record(caseId, "DISCHARGED");
    }

    void retriage(int caseId, Vitals vitals) {
        Case c = find(caseId);
        int old = c.getSeverity();
        c.reassess(vitals, scorer.score(vitals));
        audit.record(caseId, "RE-TRIAGED severity " + old + " -> " + c.getSeverity());
    }

    Case find(int caseId) {
        for (Case c : cases) {
            if (c.getCaseId() == caseId) return c;
        }
        throw new IllegalArgumentException("no case with id " + caseId);
    }

    List<Case> allCases() { return cases; }
    List<Doctor> doctorPool() { return doctors; }
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
