import java.util.Scanner;

public class Registration {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String name;
        int birthYear;
        int birthMonth;
        int birthDay;
        String reasonForVisit;

        String pastHistory;
        String medications;
        String allergies;
        String lastOralIntake;
        
        boolean hasRedFlags = false;
        String[] redFlagsList = {
            "Chest pain radiating to arm/jaw",
            "Sudden severe (thunderclap) headache",
            "Neurological deficits (facial droop, arm weakness, slurred speech)",
            "Severe acute abdominal pain"
        };
        System.out.println("          PATIENT REGISTRATION SYSTEM            ");
        System.out.print("Enter Patient Full Name: ");
        name = scanner.nextLine();

        System.out.print("Enter Birth Year (e.g., 2002): ");
        birthYear = scanner.nextInt();

        System.out.print("Enter Birth Month (1-12): ");
        birthMonth = scanner.nextInt();

        System.out.print("Enter Birth Day (1-31): ");
        birthDay = scanner.nextInt();
        scanner.nextLine(); 

        System.out.print("Why are you here today? ");
        reasonForVisit = scanner.nextLine();

        System.out.println("MEDICAL HISTORY & INTAKE");

        System.out.print("Past Medical / Surgical History: ");
        pastHistory = scanner.nextLine();

        System.out.print("Current Medications: ");
        medications = scanner.nextLine();

        System.out.print("Known Allergies: ");
        allergies = scanner.nextLine();

        System.out.print("Last Oral Intake (What and when did you last eat/drink?): ");
        lastOralIntake = scanner.nextLine();

        System.out.println("RED-FLAG SYMPTOM CHECK");
        System.out.println("Reviewing potential dangerous symptoms:");

        for (int i = 0; i < redFlagsList.length; i++) {
            System.out.print("Is the patient experiencing " + redFlagsList[i] + "? (1 = Yes, 0 = No): ");
            int response = scanner.nextInt();
            
            if (response == 1) {
                hasRedFlags = true;
            }
        }
        scanner.nextLine(); 

        if (hasRedFlags) {
            System.out.println("\n[ALERT]: Red-flag symptoms identified! Escalating priority to triage nurse.");
        } else {
            System.out.println("\nNo immediate red-flag symptoms reported.");
        }

        System.out.println("WAITING ROOM REGISTRATION COMPLETION");
        System.out.println(" Sending Intake Clerk to waiting room with a tablet to take (ID, Address, Insurance)");

        scanner.nextLine();
        String address = "";
        String emergencyContact = "";
        String insuranceInfo = "";
            System.out.println("--> Intake Clerk assigned to assist patient in waiting room.");
            
            System.out.print("Enter Address: ");
            address = scanner.nextLine();
            System.out.print("Enter Emergency Contact: ");
            emergencyContact = scanner.nextLine();
            System.out.print("Enter Insurance Information: ");
            insuranceInfo = scanner.nextLine();
        System.out.println("\n==================================================");
        System.out.println("         COMPLETED REGISTRATION SUMMARY           ");
        System.out.println("==================================================");
        System.out.println("Name             : " + name);
        System.out.println("Date of Birth    : " + birthDay + "/" + birthMonth + "/" + birthYear);
        System.out.println("Reason for Visit : " + reasonForVisit);
        System.out.println("Past History     : " + pastHistory);
        System.out.println("Medications      : " + medications);
        System.out.println("Allergies        : " + allergies);
        System.out.println("Last Oral Intake : " + lastOralIntake);
        System.out.println("Red Flags Present: " + (hasRedFlags ? "YES (URGENT)" : "NO"));
        System.out.println("--------------------------------------------------");
        System.out.println("Address          : " + address);
        System.out.println("Emergency Contact: " + emergencyContact);
        System.out.println("Insurance Info   : " + insuranceInfo);
        System.out.println("===================================================");

    
    }
}
