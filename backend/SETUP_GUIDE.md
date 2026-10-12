# EVENTTICKETINGSYSTEM — Developer Setup & Run Guide

This guide walks you through configuring, running, and testing the EVENTTICKETINGSYSTEM Spring Boot backend.

---

## Table of Contents
1. [System Requirements](#1-system-requirements)
2. [MySQL Database Setup](#2-mysql-database-setup)
3. [Configuration & Environment Variables](#3-configuration--environment-variables)
4. [Building & Running the Application](#4-building--running-the-application)
5. [Running Tests](#5-running-tests)
6. [CORS Configuration for React Frontend](#6-cors-configuration-for-react-frontend)
7. [Common Setup Issues & Troubleshooting](#7-common-setup-issues--troubleshooting)

---

## 1. System Requirements
- **Java**: Java Development Kit (JDK) 21 or later.
  Verify your installed version:
  ```powershell
  java -version
  ```
- **Maven**: Bundled with the project via Maven Wrapper (`mvnw` / `mvnw.cmd`). No standalone Maven installation is required.
- **MySQL**: MySQL 8.0 running locally on default port `3306`.
- **Operating System**: Windows / macOS / Linux.

---

## 2. MySQL Database Setup

1. Start your local MySQL service (e.g., MySQL80):
   ```powershell
   # Windows PowerShell
   Get-Service -Name MySQL80
   ```
2. Log into MySQL and create the database if it does not already exist:
   ```sql
   CREATE DATABASE IF NOT EXISTS event_ticketing_db CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
   ```
3. Hibernate will automatically manage table creation and schema updates on application startup via `spring.jpa.hibernate.ddl-auto=update`.

---

## 3. Configuration & Environment Variables

All settings are configured in [`src/main/resources/application.properties`](file:///C:/Users/VINAY/event-ticketing-system/backend/src/main/resources/application.properties) with safe development fallbacks.

### Available Configuration Properties & Environment Variables:

| Property | Environment Variable | Default Value | Description |
|---|---|---|---|
| `spring.datasource.url` | `DB_URL` | `jdbc:mysql://localhost:3306/event_ticketing_db` | JDBC connection string |
| `spring.datasource.username` | `DB_USERNAME` | `root` | MySQL user |
| `spring.datasource.password` | `DB_PASSWORD` | `""` (empty) | MySQL password |
| `server.port` | `PORT` | `4040` | Port the backend runs on |
| `jwt.secret` | `JWT_SECRET` | *(256-bit dev key)* | Secret key for signing JWTs |
| `jwt.expiration` | `JWT_EXPIRATION` | `86400000` | Token validity in ms (24 hours) |
| `cors.allowed-origins` | `CORS_ALLOWED_ORIGINS` | `http://localhost:5173,http://localhost:3000,http://127.0.0.1:5173` | Allowed React origins |

### Setting Environment Variables in Windows PowerShell:
```powershell
$env:DB_PASSWORD = "your_mysql_password"
$env:JWT_SECRET = "yourVerySecret256BitKeyForProductionUse123456!"
```

---

## 4. Building & Running the Application

Navigate to the backend directory (`C:\Users\VINAY\event-ticketing-system\backend`):

### 1. Compile the Project
```powershell
.\mvnw.cmd clean compile
```

### 2. Start the Backend Server
```powershell
.\mvnw.cmd spring-boot:run
```

Once started, the console will print:
```
Tomcat started on port 4040 (http) with context path '/'
Started BackendApplication in X.XXX seconds
```
The application is accessible at `http://localhost:4040`.

---

## 5. Running Tests

### Automated Unit & Context Tests
```powershell
.\mvnw.cmd test
```
Runs `BackendApplicationTests`, testing Spring Context initialization, JWT generation, claims parsing, expiration, and BCrypt encoding.

### Live End-to-End API Verification Script
A test script is available at `scratch/verify_api.ps1` that exercises all 16 core API workflows:
```powershell
powershell -ExecutionPolicy Bypass -File "C:\Users\VINAY\.gemini\antigravity\brain\f72d2284-56e5-4bde-bf37-0679f67c34f0\scratch\verify_api.ps1"
```

---

## 6. CORS Configuration for React Frontend

The backend allows requests from standard React development ports:
- `http://localhost:5173` (Vite)
- `http://localhost:3000` (Create React App)
- `http://127.0.0.1:5173`

If your React frontend runs on a different port (e.g. `http://localhost:8080`), you can override the property in PowerShell:
```powershell
$env:CORS_ALLOWED_ORIGINS = "http://localhost:5173,http://localhost:8080"
```
Or in `application.properties`:
```properties
cors.allowed-origins=http://localhost:5173,http://localhost:8080
```

---

## 7. Common Setup Issues & Troubleshooting

### 1. `Access denied for user 'root'@'localhost' (using password: YES)`
- **Cause**: The password supplied in `$env:DB_PASSWORD` does not match your MySQL root password.
- **Solution**: Set the correct environment variable before running:
  ```powershell
  $env:DB_PASSWORD = "correct_password"
  .\mvnw.cmd spring-boot:run
  ```

### 2. `Web server failed to start. Port 4040 was already in use.`
- **Cause**: An earlier instance of the application is already running in the background.
- **Solution**: Find and stop the process listening on port 4040:
  ```powershell
  Get-NetTCPConnection -LocalPort 4040
  Stop-Process -Id <PID> -Force
  ```

### 3. `Unknown database 'event_ticketing_db'`
- **Cause**: The MySQL database has not been initialized.
- **Solution**: Open MySQL command line and run:
  ```sql
  CREATE DATABASE event_ticketing_db;
  ```

### 4. `403 Forbidden` on POST/PUT Endpoints
- **Cause**: The request either lacks the `Authorization: Bearer <token>` header or the authenticated user does not have the `ADMIN` role.
- **Solution**:
  - For user operations, ensure the token is present in the header.
  - For administrative operations (creating events/venues/seats), log in with an account having `role = 'ADMIN'`.

