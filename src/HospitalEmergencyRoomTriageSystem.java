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

    