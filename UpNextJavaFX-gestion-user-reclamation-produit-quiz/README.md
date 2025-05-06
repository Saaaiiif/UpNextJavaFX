# Up Next JavaFX Application

This is a JavaFX application for user and reclamation management.

## Prerequisites

1. Java Development Kit (JDK) 17
2. MySQL Server 8.0+
3. Maven

## Database Setup

1. Install and start MySQL Server
2. Create a new database:
```sql
CREATE DATABASE `upnext-111`;
```
3. The application uses the following database configuration:
   - Host: localhost
   - Port: 3306
   - Database: upnext-111
   - Username: root
   - Password: (empty)

## Building and Running

1. Clone the repository
2. Navigate to the project directory
3. Build the project:
```bash
mvn clean install
```
4. Run the application:
```bash
mvn javafx:run
```

## Features

- User Management
- Reclamation Management
- Profile Management
- Admin Dashboard
- Statistics
- Chat Bot Integration
- QR Code Support
- Email Functionality
- PDF Generation

## Technology Stack

- JavaFX 20.0.2
- MySQL 8.0
- Maven
- Various libraries including:
  - iText 7 for PDF handling
  - ZXing for QR codes
  - JavaMail for email functionality
  - BCrypt for password hashing