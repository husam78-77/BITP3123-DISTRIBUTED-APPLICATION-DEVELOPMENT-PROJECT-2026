# SmartCampus Connect

**SmartCampus Connect** is a distributed campus management platform built as a group project for the BITP3123 Distributed Application Development course. It consists of five independent Spring Boot microservices and a static HTML/CSS/JavaScript frontend that together manage student profiles, course enrolments, library resources, room bookings, real-time notifications, and reporting analytics.

---

## Overview

### Purpose

SmartCampus Connect demonstrates core distributed application development concepts: independent microservices communicating over REST and TCP sockets, a SOAP web service for book availability, multithreaded concurrency control, and in-memory messaging — all without requiring external infrastructure (no Docker, no RabbitMQ installation needed).

### Main Features

| Feature | Description |
|---|---|
| Student Management | Create, view, update, and delete student profiles with validation |
| Course Enrolment | Manage courses, enrol/drop students with capacity enforcement |
| Library Booking | Add/borrow/return books; book/cancel study rooms with concurrency control |
| SOAP Book Availability | Check book availability via a SOAP/WSDL endpoint |
| Notifications | Real-time event notifications via TCP Producer–Consumer (port 9999) |
| Reporting & Analytics | Aggregated dashboard data pulled from all other services |

---

## Technology Stack

### Frontend

| Technology | Purpose |
|---|---|
| HTML5 | Page structure |
| CSS3 | Styling (`frontend/css/style.css`) |
| Vanilla JavaScript (ES6+) | API calls, DOM manipulation |

No frontend build step, no frameworks, no Node.js required.

### Backend

| Technology | Version | Purpose |
|---|---|---|
| Java | 17 | Runtime language |
| Spring Boot | 3.5.14 | Microservice framework |
| Spring Data JPA | (Boot-managed) | ORM / database access |
| Spring Web (MVC) | (Boot-managed) | REST controllers |
| Spring WS | (Boot-managed) | SOAP web service endpoint |
| Spring AMQP | (Boot-managed) | RabbitMQ integration (mocked) |
| Jakarta Validation | (Boot-managed) | Bean validation |
| Lombok | (Boot-managed) | Optional code generation |
| WSDL4J | 1.6.3 | WSDL generation for SOAP |
| JAXB2 Maven Plugin | 3.1.0 | XSD → Java class generation |
| rabbitmq-mock | 1.2.0 | In-memory RabbitMQ (no install needed) |

### Database

Each service has its own **H2 in-memory database** (no shared database, no setup required). Data is reset every time a service restarts.

### Build Tool

**Apache Maven 3.x** — multi-module project. Each service includes a Maven Wrapper (`mvnw`) so Maven does not need to be globally installed.

---

## Project Structure

```
BITP3123-DISTRIBUTED-APPLICATION-DEVELOPMENT-PROJECT-2026/
│
├── start-all-services.bat          # One-click launcher for all 5 services
│
├── frontend/                       # Static web UI (open in browser directly)
│   ├── index.html                  # Dashboard
│   ├── students.html               # Student management
│   ├── courses.html                # Course & enrolment management
│   ├── library.html                # Books, loans, and room bookings
│   ├── notifications.html          # Real-time notification log
│   ├── reports.html                # Reporting & analytics
│   ├── soap.html                   # SOAP demo page
│   ├── css/
│   │   └── style.css
│   └── js/
│       ├── common.js               # Shared API base URLs and utility functions
│       ├── students.js
│       ├── courses.js
│       ├── library.js
│       ├── notifications.js
│       ├── reports.js
│       └── soap.js
│
└── smartcampus-connect/            # Maven multi-module backend root
    ├── pom.xml                     # Parent POM (Spring Boot 3.5.14, Java 17)
    │
    ├── student-profile-service/    # Port 8081 — Student CRUD
    ├── course-enrolment-service/   # Port 8082 — Courses & Enrolment + TCP Producer
    ├── notification-service/       # Port 8083 + TCP 9999 — TCP Consumer, event log
    ├── library-booking-service/    # Port 8084 — Books, Loans, Rooms, SOAP, Thread Pool
    └── reporting-analytics-service/ # Port 8085 — Aggregated reports via REST
```

Each service follows the same internal layout:

```
<service-name>/
├── pom.xml
└── src/main/java/com/smartcampus/<domain>/
    ├── <Service>Application.java   # Spring Boot entry point
    ├── config/                     # Bean configuration
    ├── controller/                 # REST controllers
    ├── entity/                     # JPA entities
    ├── repository/                 # Spring Data repositories
    ├── service/                    # Business logic
    └── socket/                     # TCP Producer / Consumer classes
```

---

## Prerequisites

| Software | Minimum Version | Notes |
|---|---|---|
| Java (JDK) | 17 | Must be on `PATH` — check with `java -version` |
| Maven | 3.8+ | OR use the included `mvnw` wrapper (no install needed) |
| Web browser | Any modern browser | Chrome, Firefox, or Edge recommended |

> **No Docker, no RabbitMQ installation, and no database installation required.** RabbitMQ is mocked in-memory via `rabbitmq-mock` and all databases are H2 in-memory.

---

## Installation

### 1. Clone or download the repository

```bash
git clone <repository-url>
cd BITP3123-DISTRIBUTED-APPLICATION-DEVELOPMENT-PROJECT-2026
```

### 2. Verify Java 17 is installed

```bash
java -version
# Expected output: openjdk version "17.x.x" or similar
```

If Java 17 is not installed, download it from [Adoptium](https://adoptium.net/) or [Oracle](https://www.oracle.com/java/technologies/downloads/#java17).

### 3. No further installation steps are required

All Maven dependencies (Spring Boot, H2, rabbitmq-mock, etc.) are downloaded automatically on the first build. No environment variables need to be set beforehand.

---

## Configuration

All configuration is in each service's `src/main/resources/application.properties`. The defaults work out-of-the-box on a clean machine.

### Service ports

| Service | Port | H2 Console |
|---|---|---|
| student-profile-service | 8081 | `http://localhost:8081/h2-console` |
| course-enrolment-service | 8082 | `http://localhost:8082/h2-console` |
| notification-service | 8083 | `http://localhost:8083/h2-console` |
| library-booking-service | 8084 | `http://localhost:8084/h2-console` |
| reporting-analytics-service | 8085 | *(no H2 — no local database)* |

### Inter-service URLs (pre-configured)

| Setting | File | Value |
|---|---|---|
| `student.service.url` | course-enrolment-service | `http://localhost:8081` |
| `student.service.url` | reporting-analytics-service | `http://localhost:8081` |
| `enrolment.service.url` | reporting-analytics-service | `http://localhost:8082` |
| `library.service.url` | reporting-analytics-service | `http://localhost:8084` |
| `notification.socket.port` | notification-service | `9999` |

### No `.env` file is required

There are no secrets, API keys, or credentials in this project. Nothing needs to be configured before running.

---

## Build Instructions

### Build all services at once (from the `smartcampus-connect/` directory)

```bash
cd smartcampus-connect
mvn clean install
```

Or using the Maven Wrapper (no Maven installation needed):

```bash
cd smartcampus-connect
./mvnw clean install        # macOS / Linux
mvnw.cmd clean install      # Windows
```

This compiles all five services, runs the JAXB2 XSD-to-Java code generation for the library SOAP schema, and packages each service as a runnable JAR.

### Build a single service

```bash
cd smartcampus-connect/library-booking-service
mvn clean install
```

---

## Running the Application

> **Important:** The **Notification Service must start first** (it opens TCP port 9999 that other services connect to). Allow approximately 20 seconds for it to be ready before starting the others.

### Option A — One-click launcher (Windows)

Double-click `start-all-services.bat` in the project root. It starts all five services in separate Command Prompt windows with a 20-second delay after the Notification Service, then prints the service URLs.

```
start-all-services.bat
```

### Option B — Manual (any OS)

Open **five separate terminals**, one per service, in this order:

**Terminal 1 — Notification Service (start first)**
```bash
cd smartcampus-connect/notification-service
mvn spring-boot:run
```
Wait until you see `Started NotificationServiceApplication` before continuing.

**Terminal 2 — Student Profile Service**
```bash
cd smartcampus-connect/student-profile-service
mvn spring-boot:run
```

**Terminal 3 — Course Enrolment Service**
```bash
cd smartcampus-connect/course-enrolment-service
mvn spring-boot:run
```

**Terminal 4 — Library Booking Service**
```bash
cd smartcampus-connect/library-booking-service
mvn spring-boot:run
```

**Terminal 5 — Reporting Analytics Service**
```bash
cd smartcampus-connect/reporting-analytics-service
mvn spring-boot:run
```

### Memory tip (if services crash on Windows)

If Windows reports page-file exhaustion with five JVMs running simultaneously, add heap limits:

```bash
set MAVEN_OPTS=-Xms64m -Xmx256m
mvn spring-boot:run
```

### Starting the Frontend

No build or server required. Simply open any HTML file directly in a browser:

```
frontend/index.html
```

Or open via file path: `file:///C:/path/to/project/frontend/index.html`

All API base URLs are configured in [frontend/js/common.js](frontend/js/common.js) and point to `localhost` by default.

---

## API Information

### Student Profile Service — `http://localhost:8081`

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/students` | List all students |
| GET | `/api/students/{id}` | Get student by ID |
| GET | `/api/students/matric/{matricNumber}` | Get student by matric number |
| POST | `/api/students` | Create a new student (JSON body) |
| PUT | `/api/students/{id}` | Update student (JSON body) |
| DELETE | `/api/students/{id}` | Delete student |

### Course Enrolment Service — `http://localhost:8082`

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/courses` | List all courses |
| POST | `/api/courses` | Create a course (JSON body) |
| PUT | `/api/courses/{id}` | Update course (JSON body) |
| DELETE | `/api/courses/{id}` | Delete course |
| GET | `/api/enrolments` | List all enrolments |
| GET | `/api/enrolments/student/{studentId}` | Enrolments by student |
| POST | `/api/enrolments/enrol` | Enrol student (`?studentId=&courseId=&semester=`) |
| PUT | `/api/enrolments/drop` | Drop course (`?studentId=&courseId=&semester=`) |

### Notification Service — `http://localhost:8083`

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/notifications` | List all notifications |
| GET | `/api/notifications/type/{type}` | Filter by type (e.g. `LIBRARY`, `ENROLMENT`) |
| GET | `/api/notifications/status/{status}` | Filter by status |

Notifications are written to this service by other services via **TCP socket on port 9999**.

### Library Booking Service — `http://localhost:8084`

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/books` | List all books |
| GET | `/api/books/available` | List available books only |
| POST | `/api/books` | Add a book (JSON body) |
| DELETE | `/api/books/{id}` | Delete a book |
| POST | `/api/books/borrow` | Borrow a book (`?studentId=&bookId=`) |
| PUT | `/api/books/return/{loanId}` | Return a borrowed book |
| GET | `/api/books/loans` | List all book loans |
| GET | `/api/books/loans/student/{studentId}` | Loans by student |
| GET | `/api/rooms` | List all room bookings |
| POST | `/api/rooms/book` | Book a room (`?studentId=&roomId=&startTime=&endTime=`) |
| PUT | `/api/rooms/cancel/{bookingId}` | Cancel a room booking |
| POST | `/ws` | SOAP endpoint — `getBookAvailability` |
| GET | `/ws/library.wsdl` | Auto-generated WSDL |

#### SOAP Request Example

```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:lib="http://smartcampus.com/library">
   <soapenv:Header/>
   <soapenv:Body>
      <lib:getBookAvailabilityRequest>
         <lib:bookId>1</lib:bookId>
      </lib:getBookAvailabilityRequest>
   </soapenv:Body>
</soapenv:Envelope>
```

### Reporting Analytics Service — `http://localhost:8085`

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/reports/students` | Student summary |
| GET | `/api/reports/enrolments` | Enrolment summary |
| GET | `/api/reports/library` | Library activity summary |
| GET | `/api/reports/overview` | Full campus overview |

---

## Testing

Each service contains a single Spring Boot context-load test (`contextLoads()`) that verifies the application context starts without errors. There are no business logic unit tests or integration tests.

To run the tests for all services:

```bash
cd smartcampus-connect
mvn test
```

To run tests for a single service:

```bash
cd smartcampus-connect/library-booking-service
mvn test
```

---

## Repository Notes

- This repository is the official codebase for the **BITP3123 Distributed Application Development** group project (SmartCampus Connect), academic year 2026.
- **Do not commit** API keys, passwords, `.env` files, or any credentials. The project requires no external credentials to run.
- All databases are H2 in-memory. **Data does not persist between restarts.** You must re-enter all records (students, books, courses, etc.) after restarting any service.
- The `target/` directories (compiled output) are generated by Maven and should not be committed.

---

## Troubleshooting

| Problem | Likely Cause | Fix |
|---|---|---|
| "Error loading students" / all pages fail | Services not running or CORS not applied | Start all five services and wait for each to show `Started ... Application` |
| "Error connecting to server" on Add Book | Library service not started, or started before Notification Service | Restart Library Booking Service after Notification Service is up |
| "Connection refused" on SOAP tab | Library service not running or wrong namespace | Ensure service is started; the SOAP namespace must be `http://smartcampus.com/library` |
| Windows page file / OutOfMemory error | Five JVMs with large default heap | Set `MAVEN_OPTS=-Xms64m -Xmx256m` before each `mvn spring-boot:run` (already done in the `.bat` file) |
| "Student with ID X not found" on enrolment | Student service is down or student doesn't exist | Start student-profile-service first; create the student before enrolling |
| "Book not found with ID: 1" on borrow | H2 is empty after restart | Add the book first (Add Book form), then use the generated ID shown in the Library Books table |
| Port already in use | A previous service instance is still running | Find and kill the process using `netstat -ano | findstr :<PORT>` then `taskkill /PID <PID> /F` |
| Notifications not appearing | Notification service started after other services | Restart all services with Notification Service first; other services connect to TCP 9999 on startup |
| H2 Console shows no tables | Service not fully started | Wait for `Started ... Application` in the console, then refresh the H2 console |

### H2 Console Access

Connect at `http://localhost:<port>/h2-console` with:

- **JDBC URL:** `jdbc:h2:mem:<dbname>` (see table in Configuration section for each service's `<dbname>`)
- **Username:** `sa`
- **Password:** *(leave blank)*

---
## License

No license has been specified for this repository. All rights are reserved by the project authors. This codebase is intended solely for academic submission and evaluation under BITP3123 at the respective institution.