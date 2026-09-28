import java.util.Scanner;

public class HospitalEmergencyRoomTriageSystem {

    // ==========================================
    // 1. PATIENT CLASS
    // ==========================================
    static class Patient {
        private String id;
        private String name;
        private int birthYear;
        private int birthMonth;
        private int birthDay;
        private String reasonForVisit;
        private String pastHistory;
        private String medications;
        private String allergies;
        private String lastOralIntake;
        private boolean hasRedFlags;
        private String address;
        private String emergencyContact;
        private String insuranceInfo;
        private int priority; // 1: Critical, 2: High, 3: Moderate, 4: Low
        private String assignedRoom;

        public Patient(String id, String name, int birthYear, int birthMonth, int birthDay,
                       String reasonForVisit, String pastHistory, String medications,
                       String allergies, String lastOralIntake, boolean hasRedFlags,
                       String address, String emergencyContact, String insuranceInfo, int priority) {
            this.id = id;
            this.name = name;
            this.birthYear = birthYear;
            this.birthMonth = birthMonth;
            this.birthDay = birthDay;
            this.reasonForVisit = reasonForVisit;
            this.pastHistory = pastHistory;
            this.medications = medications;
            this.allergies = allergies;
            this.lastOralIntake = lastOralIntake;
            this.hasRedFlags = hasRedFlags;
            this.address = address;
            this.emergencyContact = emergencyContact;
            this.insuranceInfo = insuranceInfo;
            this.priority = priority;
            this.assignedRoom = "Unassigned";
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public int getPriority() { return priority; }
        public String getAssignedRoom() { return assignedRoom; }

        public void setAssignedRoom(String room) {
            this.assignedRoom = room;
        }

        public String getPriorityLabel() {
            if (priority == 1) return "Critical";
            if (priority == 2) return "High";
            if (priority == 3) return "Moderate";
            return "Low";
        }

        public void printSummary() {
            System.out.println("\n==================================================");
            System.out.println("         COMPLETED REGISTRATION SUMMARY           ");
            System.out.println("==================================================");
            System.out.println("ID               : " + id);
            System.out.println("Name             : " + name);
            System.out.println("Date of Birth    : " + birthDay + "/" + birthMonth + "/" + birthYear);
            System.out.println("Reason for Visit : " + reasonForVisit);
            System.out.println("Past History     : " + pastHistory);
            System.out.println("Medications      : " + medications);
            System.out.println("Allergies        : " + allergies);
            System.out.println("Last Oral Intake : " + lastOralIntake);
            System.out.println("Red Flags Present: " + (hasRedFlags ? "YES (URGENT)" : "NO"));
            System.out.println("Assigned Priority: " + priority + " (" + getPriorityLabel() + ")");
            System.out.println("Assigned Room    : " + assignedRoom);
            System.out.println("--------------------------------------------------");
            System.out.println("Address          : " + address);
            System.out.println("Emergency Contact: " + emergencyContact);
            System.out.println("Insurance Info   : " + insuranceInfo);
            System.out.println("==================================================");
        }
    }

    // ==========================================
    // 2. GLOBAL SYSTEM DATA STRUCTURES
    // ==========================================
    private static Patient[] patientQueue = new Patient[50];
    private static int patientCount = 0;
    private static int idCounter = 1000;

    // Emergency Rooms (Critical/High) & Waiting Rooms (Moderate/Low)
    private static String[] rooms = {
        "Emergency Room 1 (Critical/High)", 
        "Emergency Room 2 (Critical/High)", 
        "Waiting Room A (Moderate/Low)", 
        "Waiting Room B (Moderate/Low)"
    };
    private static boolean[] roomOccupied = new boolean[4];

    // ==========================================
    // 3. MAIN MENU EXECUTION
    // ==========================================
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n=================================");
            System.out.println(" HOSPITAL TRIAGE & ROOM ALLOCATION");
            System.out.println("=================================");
            System.out.println("1. Register New Patient");
            System.out.println("2. Allocate Room to Next Patient");
            System.out.println("3. Display Waiting Queue");
            System.out.println("4. View Room Statuses");
            System.out.println("5. Discharge Patient / Free Room");
            System.out.println("6. Exit");
            System.out.print("Select an option (1-6): ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Clear buffer

            if (choice == 1) {
                registerPatient(scanner);
            } else if (choice == 2) {
                allocateRoom();
            } else if (choice == 3) {
                displayQueue();
            } else if (choice == 4) {
                displayRooms();
            } else if (choice == 5) {
                freeRoom(scanner);
            } else if (choice == 6) {
                running = false;
                System.out.println("\nExiting system. Goodbye!");
            } else {
                System.out.println("\n[ERROR] Invalid choice. Try again.");
            }
        }

        scanner.close();
    }
// ==========================================
    // 4. CORE SYSTEM METHODS
    // ==========================================

    // METHOD 1: Patient Registration (Using Integrated Clinical Intake Logic)
    public static void registerPatient(Scanner scanner) {
        if (patientCount >= patientQueue.length) {
            System.out.println("\n[ERROR] Queue full! Cannot register more patients.");
            return;
        }

        idCounter++;
        String id = "P" + idCounter;

        // --- Standard variables for patient registration ---
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

        // Visual check array for Red-Flag symptoms
        String[] redFlagsList = {
            "Chest pain radiating to arm/jaw",
            "Sudden severe (thunderclap) headache",
            "Neurological deficits (facial droop, arm weakness, slurred speech)",
            "Severe acute abdominal pain"
        };

        System.out.println("\n           PATIENT REGISTRATION SYSTEM            ");
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

        System.out.println("\nMEDICAL HISTORY & INTAKE");

        System.out.print("Past Medical / Surgical History: ");
        pastHistory = scanner.nextLine();

        System.out.print("Current Medications: ");
        medications = scanner.nextLine();

        System.out.print("Known Allergies: ");
        allergies = scanner.nextLine();

        System.out.print("Last Oral Intake (What and when did you last eat/drink?): ");
        lastOralIntake = scanner.nextLine();

        System.out.println("\nRED-FLAG SYMPTOM CHECK");
        System.out.println("Reviewing potential dangerous symptoms:");

        for (int i = 0; i < redFlagsList.length; i++) {
            System.out.print("Is the patient experiencing " + redFlagsList[i] + "? (1 = Yes, 0 = No): ");
            int response = scanner.nextInt();
            
            if (response == 1) {
                hasRedFlags = true;
            }
        }
        scanner.nextLine(); // Clear buffer

        // Priority Determination Logic
        int priority;
        if (hasRedFlags) {
            System.out.println("\n[ALERT]: Red-flag symptoms identified! Escalating priority to triage nurse.");
            System.out.println("Select Priority for Emergency Red-Flag Patient:");
            System.out.println(" 1 - Critical (Immediate emergency room allocation)");
            System.out.println(" 2 - High (Urgent emergency room allocation)");
            System.out.print("Select Priority (1 or 2): ");
            priority = scanner.nextInt();
            scanner.nextLine();

            if (priority != 1 && priority != 2) {
                priority = 1; // Default critical on invalid input
            }
        } else {
            System.out.println("\nNo immediate red-flag symptoms reported.");
            System.out.println("Select Priority for Standard Patient:");
            System.out.println(" 3 - Moderate (Waiting room allocation)");
            System.out.println(" 4 - Low (Waiting room allocation)");
            System.out.print("Select Priority (3 or 4): ");
            priority = scanner.nextInt();
            scanner.nextLine();

            if (priority != 3 && priority != 4) {
                priority = 4; // Default low on invalid input
            }
        }

        System.out.println("\nWAITING ROOM REGISTRATION COMPLETION");
        System.out.println(" Sending Intake Clerk to waiting room with a tablet to take (ID, Address, Insurance)");

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

        // Create & Store Patient Object
        Patient newPatient = new Patient(id, name, birthYear, birthMonth, birthDay, 
                                         reasonForVisit, pastHistory, medications, 
                                         allergies, lastOralIntake, hasRedFlags, 
                                         address, emergencyContact, insuranceInfo, priority);

        patientQueue[patientCount] = newPatient;
        patientCount++;

        // Sort Queue by Priority
        sortByPriority();

        // Print Completed Summary
        newPatient.printSummary();
    }

    // METHOD 2: Room Allocation
    public static void allocateRoom() {
        if (patientCount == 0) {
            System.out.println("\n[INFO] No waiting patients to allocate.");
            return;
        }

        Patient patientToAssign = patientQueue[0];
        int priority = patientToAssign.getPriority();
        int targetRoomIndex = -1;

        // Emergency Rooms (Index 0, 1) assigned to Critical (1) & High (2)
        if (priority == 1 || priority == 2) {
            for (int i = 0; i <= 1; i++) {
                if (!roomOccupied[i]) {
                    targetRoomIndex = i;
                    break;
                }
            }
            if (targetRoomIndex == -1) {
                System.out.println("\n[WARNING] All Emergency Rooms occupied! Cannot allocate " + 
                                   patientToAssign.getName() + " (" + patientToAssign.getPriorityLabel() + ").");
                return;
            }
        } 
        // Waiting Rooms (Index 2, 3) assigned to Moderate (3) & Low (4)
        else {
            for (int i = 2; i <= 3; i++) {
                if (!roomOccupied[i]) {
                    targetRoomIndex = i;
                    break;
                }
            }
            if (targetRoomIndex == -1) {
                System.out.println("\n[WARNING] All Waiting Rooms occupied! Cannot allocate " + 
                                   patientToAssign.getName() + " (" + patientToAssign.getPriorityLabel() + ").");
                return;
            }
        }

        patientToAssign.setAssignedRoom(rooms[targetRoomIndex]);
        roomOccupied[targetRoomIndex] = true;

        System.out.println("\n[ALLOCATED] Patient " + patientToAssign.getName() + 
                           " (" + patientToAssign.getPriorityLabel() + ") assigned to " + rooms[targetRoomIndex]);

        // Shift Queue Left
        for (int i = 0; i < patientCount - 1; i++) {
            patientQueue[i] = patientQueue[i + 1];
        }
        patientQueue[patientCount - 1] = null;
        patientCount--;
    }

    // Helper: Bubble Sort Array by Priority Order
    private static void sortByPriority() {
        for (int i = 0; i < patientCount - 1; i++) {
            for (int j = 0; j < patientCount - i - 1; j++) {
                if (patientQueue[j].getPriority() > patientQueue[j + 1].getPriority()) {
                    Patient temp = patientQueue[j];
                    patientQueue[j] = patientQueue[j + 1];
                    patientQueue[j + 1] = temp;
                }
            }
        }
    }

    // METHOD 3: Display Queue
    public static void displayQueue() {
        if (patientCount == 0) {
            System.out.println("\n[INFO] Queue is currently empty.");
            return;
        }

        System.out.println("\n============== CURRENT WAITING QUEUE ==============");
        for (int i = 0; i < patientCount; i++) {
            System.out.println((i + 1) + ". ID: " + patientQueue[i].getId() +
                               " | Name: " + patientQueue[i].getName() +
                               " | Priority: " + patientQueue[i].getPriority() + 
                               " (" + patientQueue[i].getPriorityLabel() + ")");
        }
        System.out.println("==================================================");
    }

    // METHOD 4: View Room Statuses
    public static void displayRooms() {
        System.out.println("\n============== ROOM STATUSES ==============");
        for (int i = 0; i < rooms.length; i++) {
            String status = roomOccupied[i] ? "OCCUPIED" : "AVAILABLE";
            System.out.println((i + 1) + ". " + rooms[i] + " : [" + status + "]");
        }
        System.out.println("==================================================");
    }

    // METHOD 5: Discharge / Free Room
    public static void freeRoom(Scanner scanner) {
        displayRooms();
        System.out.print("Select room number to free up (1-4): ");
        int roomNum = scanner.nextInt();
        scanner.nextLine();

        if (roomNum >= 1 && roomNum <= rooms.length) {
            int index = roomNum - 1;
            if (roomOccupied[index]) {
                roomOccupied[index] = false;
                System.out.println("\n[SUCCESS] " + rooms[index] + " is now free.");
            } else {
                System.out.println("\n[INFO] Room is already vacant.");
            }
        } else {
            System.out.println("\n[ERROR] Invalid room number.");
        }
    }
}
    
