// ER Triage System - Week 4: loops over queue and doctors
// Compile and run: javac week04.java && java Main

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// ===== Main.java =====
// Week 4: loops. One loop scans the queue for the most urgent waiting patient,
// another scans the doctor pool for a free doctor, and the report loops over every patient.
class Main {
    static final String WAITING = "WAITING", IN_TREATMENT = "IN_TREATMENT", DISCHARGED = "DISCHARGED";

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        List<String> names = new ArrayList<>();
        List<Integer> severities = new ArrayList<>();
        List<String> statuses = new ArrayList<>();
        List<String> doctors = new ArrayList<>(List.of("Dr. Menon", "Dr. Kapoor"));
        List<Integer> doctorPatient = new ArrayList<>(List.of(-1, -1));   // -1 means free

        System.out.println("=== ER Triage ===");
        while (true) {
            System.out.println("1) Admit  2) Call next  3) Discharge  4) Re-triage  5) Report  6) Exit");
            System.out.print("Choice: ");
            String choice = in.nextLine().trim();

            if (choice.equals("1")) {
                System.out.print("Name: ");
                String name = in.nextLine().trim();
                System.out.print("SYS DIA HR TEMP (e.g. 90 60 130 36.8): ");
                String[] v = in.nextLine().trim().split("\\s+");
                double sys = Double.parseDouble(v[0]);
                double hr = Double.parseDouble(v[2]);
                double temp = Double.parseDouble(v[3]);

                int severity;
                if (sys <= 90 || hr >= 130 || hr <= 40 || temp >= 40.0) severity = 1;   // CRITICAL
                else if (sys < 110 || hr >= 110 || temp >= 39.0) severity = 2;          // URGENT
                else if (hr >= 100 || temp >= 38.0) severity = 3;                       // SEMI-URGENT
                else severity = 4;                                                      // MINOR

                names.add(name);
                severities.add(severity);
                statuses.add(WAITING);
                System.out.println("-> Patient #" + names.size() + " severity " + severity);
            } else if (choice.equals("2")) {
                int best = -1;                                    // loop 1: scan the queue
                for (int i = 0; i < names.size(); i++) {
                    if (statuses.get(i).equals(WAITING)
                            && (best == -1 || severities.get(i) < severities.get(best))) {
                        best = i;      // strictly lower severity wins; ties keep the earlier arrival
                    }
                }
                int doc = -1;                                     // loop 2: scan the doctor pool
                for (int d = 0; d < doctors.size(); d++) {
                    if (doctorPatient.get(d) == -1) {
                        doc = d;
                        break;
                    }
                }
                if (best == -1) {
                    System.out.println("No patients waiting.");
                } else if (doc == -1) {
                    System.out.println("No free doctor - " + names.get(best) + " keeps waiting.");
                } else {
                    statuses.set(best, IN_TREATMENT);
                    doctorPatient.set(doc, best);
                    System.out.println("-> Calling " + names.get(best) + " (sev " + severities.get(best)
                            + ") to " + doctors.get(doc));
                }
            } else if (choice.equals("3")) {
                System.out.print("Patient #: ");
                int p = Integer.parseInt(in.nextLine().trim()) - 1;
                if (p < 0 || p >= names.size()) {
                    System.out.println("No such patient.");
                } else if (!statuses.get(p).equals(IN_TREATMENT)) {
                    System.out.println("Invalid transition: " + names.get(p) + " is " + statuses.get(p));
                } else {
                    statuses.set(p, DISCHARGED);
                    for (int d = 0; d < doctors.size(); d++) {
                        if (doctorPatient.get(d) == p) doctorPatient.set(d, -1);
                    }
                    System.out.println("-> " + names.get(p) + " discharged.");
                }
            } else if (choice.equals("5")) {
                int waiting = 0;
                System.out.println("# name            severity  status");
                for (int i = 0; i < names.size(); i++) {
                    System.out.printf("%d %-15s %-9d %s%n", i + 1, names.get(i), severities.get(i), statuses.get(i));
                    if (statuses.get(i).equals(WAITING)) waiting++;
                }
                System.out.println("in queue: " + waiting);
                for (int d = 0; d < doctors.size(); d++) {
                    int p = doctorPatient.get(d);
                    System.out.println(doctors.get(d) + ": " + (p == -1 ? "free" : "busy with #" + (p + 1)));
                }
            } else if (choice.equals("6")) {
                System.out.println("Shift closed.");
                break;
            } else {
                System.out.println("Not built yet.");
            }
        }
    }
}
