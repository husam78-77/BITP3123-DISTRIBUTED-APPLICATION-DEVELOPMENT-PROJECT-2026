@echo off
echo Starting SmartCampus Connect Services...
echo.

set BASE=%~dp0smartcampus-connect

rem Limit each JVM to 256MB heap so 5 services fit in RAM comfortably
set MAVEN_OPTS=-Xms64m -Xmx256m

echo [1/5] Starting Notification Service (port 8083 + TCP 9999)...
start "Notification Service" cmd /k "set MAVEN_OPTS=-Xms64m -Xmx256m && cd /d "%BASE%\notification-service" && mvn spring-boot:run"

echo Waiting 20 seconds for Notification Service to initialize...
timeout /t 20 /nobreak > nul

echo [2/5] Starting Student Profile Service (port 8081)...
start "Student Profile Service" cmd /k "set MAVEN_OPTS=-Xms64m -Xmx256m && cd /d "%BASE%\student-profile-service" && mvn spring-boot:run"

echo [3/5] Starting Library Booking Service (port 8084)...
start "Library Booking Service" cmd /k "set MAVEN_OPTS=-Xms64m -Xmx256m && cd /d "%BASE%\library-booking-service" && mvn spring-boot:run"

echo [4/5] Starting Course Enrolment Service (port 8082)...
start "Course Enrolment Service" cmd /k "set MAVEN_OPTS=-Xms64m -Xmx256m && cd /d "%BASE%\course-enrolment-service" && mvn spring-boot:run"

echo [5/5] Starting Reporting Analytics Service (port 8085)...
start "Reporting Analytics Service" cmd /k "set MAVEN_OPTS=-Xms64m -Xmx256m && cd /d "%BASE%\reporting-analytics-service" && mvn spring-boot:run"

echo.
echo All services are starting in separate windows.
echo Wait for each window to show "Started ... Application" before using the frontend.
echo.
echo Service URLs:
echo   Students  -^> http://localhost:8081/api/students
echo   Courses   -^> http://localhost:8082/api/courses
echo   Notifs    -^> http://localhost:8083/api/notifications
echo   Library   -^> http://localhost:8084/api/books
echo   Reports   -^> http://localhost:8085/api/reports/overview
echo   SOAP WSDL -^> http://localhost:8084/ws/library.wsdl
echo.
pause
