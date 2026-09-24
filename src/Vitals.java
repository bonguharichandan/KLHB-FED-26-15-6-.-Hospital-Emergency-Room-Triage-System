import java.util.Scanner;

public class Vitals {

    // These are the fields the error was screaming about
    int heartRate;
    int respiratoryRate;
    int systolicBP;
    int diastolicBP;
    double temperature;
    int spo2;
    int painScore;
    String consciousness;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Vitals v = new Vitals();

        System.out.println("==================================================");
        System.out.println("          VITAL SIGNS & FINAL ACUITY");
        System.out.println("==================================================\n");

        v.heartRate = getIntInRange(sc, "Heart Rate (bpm): ", 20, 250);
        v.respiratoryRate = getIntInRange(sc, "Respiratory Rate (breaths/min): ", 5, 60);
        v.systolicBP = getIntInRange(sc, "Systolic BP (mmHg): ", 50, 300);
        v.diastolicBP = getIntInRange(sc, "Diastolic BP (mmHg): ", 20, 200);
        v.temperature = getDoubleInRange(sc, "Temperature (°C): ", 30.0, 43.0);
        v.spo2 = getIntInRange(sc, "SpO2 (%): ", 50, 100);
        v.painScore = getIntInRange(sc, "Pain Score (0-10): ", 0, 10);
        v.consciousness = getAVPU(sc);

        String finalPriority = calculateFinalPriority(v);

        printVitalsSummary(v, finalPriority);

        sc.close();
    }

    static int getIntInRange(Scanner sc, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            try {
                int value = Integer.parseInt(sc.nextLine().trim());
                if (value >= min && value <= max) return value;
                System.out.println("  → Value must be between " + min + " and " + max);
            } catch (NumberFormatException e) {
                System.out.println("  → Enter a valid number.");
            }
        }
    }

    static double getDoubleInRange(Scanner sc, String prompt, double min, double max) {
        while (true) {
            System.out.print(prompt);
            try {
                double value = Double.parseDouble(sc.nextLine().trim());
                if (value >= min && value <= max) return value;
                System.out.println("  → Value must be between " + min + " and " + max);
            } catch (NumberFormatException e) {
                System.out.println("  → Enter a valid number.");
            }
        }
    }

    static String getAVPU(Scanner sc) {
        while (true) {
            System.out.print("Consciousness (A=Alert, V=Verbal, P=Pain, U=Unresponsive): ");
            String input = sc.nextLine().trim().toUpperCase();
            if (input.equals("A") || input.equals("V") || input.equals("P") || input.equals("U")) {
                return input;
            }
            System.out.println("  → Enter only A, V, P or U.");
        }
    }

    static String calculateFinalPriority(Vitals v) {
        if (v.consciousness.equals("U") || v.consciousness.equals("P") ||
            v.heartRate < 40 || v.heartRate > 150 ||
            v.respiratoryRate < 8 || v.respiratoryRate > 35 ||
            v.systolicBP < 80 ||
            v.spo2 < 90 ||
            v.temperature >= 41.0 || v.temperature <= 34.0) {
            return "IMMEDIATE / CRITICAL (ESI 1-2) → RESUSCITATION BAY NOW";
        }

        if (v.heartRate > 120 || v.heartRate < 50 ||
            v.respiratoryRate > 28 || v.respiratoryRate < 10 ||
            v.systolicBP < 90 || v.systolicBP > 200 ||
            v.spo2 < 94 ||
            v.painScore >= 8 ||
            v.temperature >= 39.5 || v.temperature <= 35.0 ||
            v.consciousness.equals("V")) {
            return "URGENT (ESI 2-3) → SEE WITHIN 15 MINUTES";
        }

        if (v.painScore >= 5 || v.spo2 < 96 || v.heartRate > 100) {
            return "SEMI-URGENT (ESI 3-4)";
        }

        return "NON-URGENT (ESI 4-5)";
    }

    static void printVitalsSummary(Vitals v, String priority) {
        System.out.println("\n==================================================");
        System.out.println("              VITAL SIGNS SUMMARY");
        System.out.println("==================================================");
        System.out.println("Heart Rate        : " + v.heartRate + " bpm");
        System.out.println("Respiratory Rate  : " + v.respiratoryRate + " /min");
        System.out.println("Blood Pressure    : " + v.systolicBP + "/" + v.diastolicBP + " mmHg");
        System.out.println("Temperature       : " + v.temperature + " °C");
        System.out.println("SpO2              : " + v.spo2 + " %");
        System.out.println("Pain Score        : " + v.painScore + "/10");
        System.out.println("Consciousness     : " + v.consciousness);
        System.out.println("--------------------------------------------------");
        System.out.println("FINAL PRIORITY    : " + priority);
        System.out.println("==================================================");

        if (priority.contains("IMMEDIATE") || priority.contains("URGENT")) {
            System.out.println("\n*** ACTION REQUIRED ***");
            System.out.println("→ Move patient to treatment area immediately.");
            System.out.println("→ Notify attending / senior nurse now.");
            System.out.println("→ Do not leave patient in waiting room.");
        } else {
            System.out.println("\nPatient can remain in waiting area with reassessment.");
        }
    }
}