# KLHB-FED-26-15-6-.-Hospital-Emergency-Room-Triage-System
# Hospital Emergency-Room Triage System

## Project Information

**Project Title:** Hospital Emergency-Room Triage System
**Programming Language:** Java
**Supervisor:** K. Rakesh

### Team Members

| Name                  | ID Number  |
| --------------------- | ---------- |
| BONGU HARI CHANDAN    | 2620030271 |
| SADDHARU DINESH KUMAR | 2620090076 |
| D. RAGHURAMAKRISHNA   | 2620030446 |

---

## Abstract

The **Hospital Emergency-Room Triage System** is a Java-based application designed to assist hospitals in organizing and prioritizing patients arriving at an emergency department. In emergency situations, patients need to be assessed according to the severity and urgency of their medical condition rather than simply being treated in the order in which they arrive.

The system maintains patient information and assigns a priority level based on the severity of the patient's condition. Patients with more critical conditions are placed ahead of those with less urgent conditions, helping emergency-room staff manage the treatment queue more effectively. The system can support operations such as registering patients, assigning priority, displaying the waiting queue, and processing patients according to their priority.

The project demonstrates the application of **Java programming concepts, object-oriented programming, data structures, conditional logic, and priority-based queue management** to a practical healthcare scenario. The system is intended as an academic project and is not a substitute for professional medical judgment or a clinical hospital system.

---

## Technology Used

* **Programming Language:** Java
* **Development Environment:** Any Java-compatible IDE
* **Java Version:** Java 8 or later
* **Data Structure:** Priority Queue
* **Version Control:** Git and GitHub

---

## Project Objectives

1. To develop a Java-based emergency-room patient management system.
2. To prioritize patients according to the urgency of their medical condition.
3. To demonstrate the use of priority-based data structures in a real-world application.
4. To provide an organized method for registering and processing emergency patients.
5. To apply object-oriented programming concepts in Java.
6. To maintain a clear and verifiable development history using GitHub.

---

## Features

* Patient registration
* Patient information management
* Emergency severity/priority assignment
* Priority-based patient queue
* Display of waiting patients
* Processing of the highest-priority patient
* Basic queue management
* Java-based object-oriented implementation

---

## Folder Structure

The repository follows the required project structure:

```text
Hospital-Emergency-Room-Triage-System/
│
├── src/
│   └── Java source code
│
├── docs/
│   └── Project documentation
│
├── data/
│   └── Data files or documented data-source references
│
├── results/
│   └── Output screenshots and results
│
├── reports/
│   └── Project reports and phase deliverables
│
└── README.md
```

---

## Setup Instructions

### 1. Install Java

Install **Java JDK 8 or later** on your system.

Verify the installation using:

```bash
java -version
```

and:

```bash
javac -version
```

### 2. Clone the Repository

```bash
git clone <repository-url>
```

Move into the project directory:

```bash
cd Hospital-Emergency-Room-Triage-System
```

### 3. Compile the Project

If the Java source files are directly inside the `src` directory:

```bash
javac src/*.java
```

### 4. Run the Program

Run the main Java class using:

```bash
java -cp src Main
```

> Replace `Main` with the actual main class name if a different class is used.

---

## Execution

The general workflow of the system is:

```text
Start
  ↓
Register Patient
  ↓
Enter Patient Details
  ↓
Determine Emergency Priority
  ↓
Add Patient to Priority Queue
  ↓
Display Waiting Patients
  ↓
Process Highest-Priority Patient
  ↓
Update Queue
  ↓
End
```

---

## Priority System

The system organizes patients according to the urgency of their condition.

For example:

| Priority | Category | Description                  |
| -------- | -------- | ---------------------------- |
| 1        | Critical | Requires immediate attention |
| 2        | High     | Requires urgent attention    |
| 3        | Moderate | Requires timely treatment    |
| 4        | Low      | Less urgent condition        |

A lower priority number represents a higher level of urgency in the system.

---

## Current Phase Status

**Current Status:** Development Phase

The project is currently being developed and tested. Core Java implementation, patient management, priority-based queue handling, testing, documentation, and project reports will be progressively added throughout the project phases.

### Phase Progress

* [ ] Project setup
* [ ] Basic Java implementation
* [ ] Patient registration
* [ ] Priority-based queue implementation
* [ ] Patient processing
* [ ] Testing and debugging
* [ ] Documentation
* [ ] Review 1
* [ ] Review 2
* [ ] Final submission

---

## GitHub Contribution Guidelines

All team members will contribute using their **individual GitHub accounts** so that contributions can be verified through the repository commit history.

The team will maintain:

* Progressive commits throughout development
* At least one meaningful commit per week
* Separate contributions from each team member
* Tags for major phase deliverables

Example tags:

```text
review-1
review-2
final
```

---

## Team

### BONGU HARI CHANDAN

**ID:** 2620030271

### SADDHARU DINESH KUMAR

**ID:** 2620090076

### D. RAGHURAMAKRISHNA

**ID:** 2620030446

### Supervisor

**K. Rakesh**

---

## Academic Project Disclaimer

This project is developed for academic purposes to demonstrate software development and Java programming concepts. It is not intended to provide medical advice, determine actual medical emergencies, or replace qualified healthcare professionals or hospital triage protocols.
