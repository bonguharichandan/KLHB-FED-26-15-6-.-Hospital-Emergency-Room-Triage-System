// ER Triage System - Week 6: arrays
// Compile and run: javac week06.java && java Main

import java.util.Scanner;

// ===== Main.java =====
// Week 6: arrays. Fixed-size arrays hold the patients, one vitals row per patient, and band counters per severity.
class Main {
    static final int MAX = 50;
    static final String WAITING = "WAITING", IN_TREATMENT = "IN_TREATMENT", DISCHARGED = "DISCHARGED";
    static final String[] BAND = {"", "CRITICAL", "URGENT", "SEMI-URGENT", "MINOR"};
    static final Scanner in = new Scanner(System.in);

    static final String[] names = new String[MAX];
    static final double[][] vitals = new double[MAX][4];   // one row of vitals per patient
    static final int[] severities = new int[MAX];
    static final String[] statuses = new String[MAX];
    static final int[] bandCounts = new int[5];            // bandCounts[1..4] = patients per band
    static final String[] doctors = {"Dr. Menon", "Dr. Kapoor"};
    static final int[] doctorPatient = {-1, -1};           // -1 means free
    static int count = 0;

    public static void main(String[] args) {
        System.out.println("=== ER Triage ===");
        while (true) {
            System.out.println("1) Admit  2) Call next  3) Discharge  4) Re-triage  5) Report  6) Exit");
            System.out.print("Choice: ");
            String choice = in.nextLine().trim();
            switch (choice) {
                case "1" -> admit();
                case "2" -> callNext();
                case "3" -> discharge();
                case "4" -> retriage();
                case "5" -> report();
                case "6" -> {
                    System.out.println("Shift closed.");
                    return;
                }
                default -> System.out.println("Please pick 1-6.");
            }
        }
    }

    static void admit() {
        if (count == MAX) {
            System.out.println("Department is full.");
            return;
        }
        System.out.print("Name: ");
        names[count] = in.nextLine().trim();
        vitals[count] = readVitals();
        severities[count] = severityOf(vitals[count]);
        statuses[count] = WAITING;
        bandCounts[severities[count]]++;
        count++;
        System.out.println("-> Patient #" + count + " severity " + severities[count - 1]);
    }

    static double[] readVitals() {
        System.out.print("SYS DIA HR TEMP (e.g. 90 60 130 36.8): ");
        String[] parts = in.nextLine().trim().split("\\s+");
        double[] v = new double[4];
        for (int i = 0; i < 4; i++) v[i] = Double.parseDouble(parts[i]);
        return v;
    }

    static int severityOf(double[] v) {
        double sys = v[0], hr = v[2], temp = v[3];
        if (sys <= 90 || hr >= 130 || hr <= 40 || temp >= 40.0) return 1;   // CRITICAL
        if (sys < 110 || hr >= 110 || temp >= 39.0) return 2;               // URGENT
        if (hr >= 100 || temp >= 38.0) return 3;                            // SEMI-URGENT
        return 4;                                                           // MINOR
    }

    static void callNext() {
        int best = mostUrgent(0, -1);
        int doc = freeDoctor();
        if (best == -1) {
            System.out.println("No patients waiting.");
        } else if (doc == -1) {
            System.out.println("No free doctor - " + names[best] + " keeps waiting.");
        } else {
            statuses[best] = IN_TREATMENT;
            doctorPatient[doc] = best;
            System.out.println("-> Calling " + names[best] + " (sev " + severities[best] + ") to " + doctors[doc]);
        }
    }

    // Recursion over the array: keep the most urgent waiting index seen so far.
    static int mostUrgent(int i, int best) {
        if (i == count) return best;
        boolean better = statuses[i].equals(WAITING)
                && (best == -1 || severities[i] < severities[best]);
        return mostUrgent(i + 1, better ? i : best);
    }

    static int freeDoctor() {
        for (int d = 0; d < doctors.length; d++) {
            if (doctorPatient[d] == -1) return d;
        }
        return -1;
    }

    static int askPatient() {
        System.out.print("Patient #: ");
        return Integer.parseInt(in.nextLine().trim()) - 1;
    }

    static void discharge() {
        int p = askPatient();
        if (p < 0 || p >= count) {
            System.out.println("No such patient.");
        } else if (!statuses[p].equals(IN_TREATMENT)) {
            System.out.println("Invalid transition: " + names[p] + " is " + statuses[p]);
        } else {
            statuses[p] = DISCHARGED;
            for (int d = 0; d < doctors.length; d++) {
                if (doctorPatient[d] == p) doctorPatient[d] = -1;
            }
            System.out.println("-> " + names[p] + " discharged.");
        }
    }

    static void retriage() {
        int p = askPatient();
        if (p < 0 || p >= count) {
            System.out.println("No such patient.");
        } else if (!statuses[p].equals(WAITING)) {
            System.out.println("Only waiting patients can be re-triaged.");
        } else {
            bandCounts[severities[p]]--;
            vitals[p] = readVitals();
            severities[p] = severityOf(vitals[p]);
            bandCounts[severities[p]]++;
            System.out.println("-> " + names[p] + " now severity " + severities[p]);
        }
    }

    static void report() {
        int waiting = 0;
        System.out.println("# name            severity  status");
        for (int i = 0; i < count; i++) {
            System.out.printf("%d %-15s %-9d %s%n", i + 1, names[i], severities[i], statuses[i]);
            if (statuses[i].equals(WAITING)) waiting++;
        }
        System.out.println("in queue: " + waiting);
        for (int b = 1; b <= 4; b++) System.out.println("  " + BAND[b] + " patients: " + bandCounts[b]);
        for (int d = 0; d < doctors.length; d++) {
            int p = doctorPatient[d];
            System.out.println(doctors[d] + ": " + (p == -1 ? "free" : "busy with #" + (p + 1)));
        }
    }
}
