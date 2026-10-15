// ER Triage System - Week 2: primitive types
// Compile and run: javac week02.java && java Main

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// ===== Main.java =====
// Week 2: primitive types. int for severity and age, double for vitals, boolean for the doctor's busy flag.
class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        List<String> queue = new ArrayList<>();
        boolean drMenonBusy = false;

        System.out.println("=== ER Triage ===");
        while (true) {
            System.out.println("1) Admit  2) Call next  3) Discharge  4) Re-triage  5) Report  6) Exit");
            System.out.print("Choice: ");
            String choice = in.nextLine().trim();

            if (choice.equals("1")) {
                System.out.print("Name: ");
                String name = in.nextLine().trim();
                System.out.print("Age: ");
                int age = Integer.parseInt(in.nextLine().trim());
                System.out.print("SYS DIA HR (e.g. 90 60 130): ");
                String[] v = in.nextLine().trim().split("\\s+");
                double sys = Double.parseDouble(v[0]);
                double dia = Double.parseDouble(v[1]);
                double hr = Double.parseDouble(v[2]);
                System.out.print("Severity (1-4): ");
                int severity = Integer.parseInt(in.nextLine().trim());

                queue.add(String.format("%s (age %d) BP %.0f/%.0f HR %.0f -> severity %d",
                        name, age, sys, dia, hr, severity));
                System.out.println("Waiting queue: " + queue);
            } else if (choice.equals("2")) {
                if (queue.isEmpty()) {
                    System.out.println("No patients waiting.");
                } else if (drMenonBusy) {
                    System.out.println("Dr. Menon is busy.");
                } else {
                    drMenonBusy = true;
                    System.out.println("-> Calling " + queue.remove(0));
                }
            } else if (choice.equals("3")) {
                drMenonBusy = false;
                System.out.println("Dr. Menon is free.");
            } else if (choice.equals("6")) {
                System.out.println("Shift closed.");
                break;
            } else {
                System.out.println("Not built yet.");
            }
        }
    }
}
