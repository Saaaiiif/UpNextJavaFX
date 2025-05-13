# Up Next JavaFX Application

This is a JavaFX application for art, event, user, and reclamation management, developed as part of the PIDEV 3A coursework at Esprit School of Engineering for the 2024-2025 academic year. Up-Next provides a robust platform for artists to create and showcase artworks, clients to purchase and book events, and administrators to manage users and system analytics.

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
   - Port: 3307
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
==>Clone the repository:
git clone https://github.com/Saaaiiif/UpNextJavaFX.git

==>Navigate to the project directory:
cd upnext

=>Build the project:
mvn clean install

=>Run the application:

mvn javafx:run

## Features

- User Management: Add, delete, activate/deactivate users, and export user data to PDF.
- Reclamation Management: Submit and process user complaints with admin oversight.
- Profile Management: Manage user profiles with image uploads and view verified artists.
- Admin Dashboard: Centralized interface for managing users, artworks, events, and statistics.
- Statistics: Visualize category-based analytics through charts.
- Chat Bot Integration: Interactive AI chatbot powered by OpenRouter for user assistance.
- QR Code Support: Generate QR codes for products in the shopping cart.
- Email Functionality: Send password reset emails via JavaMail.
- PDF Generation: Export user reports to PDF using iText and Apache PDFBox.
- Artwork Management: Create, edit, delete, validate, and purchase artworks with AI image analysis via Clarifai.
- Event Management: Create, modify, reserve events, and associate locations using Mapbox geolocation.
- AI Image Generation: Generate artwork images using Hugging Face and Replicate APIs.
- Secure Payment: Process payments securely via a WebView interface.
- Search Functionality: Dynamically search for artworks, events, and users.

## Technology Stack

- JavaFX 20.0.2
- MySQL 
- Maven

## APIs and Libraries

OpenRouter: AI-powered chatbot for user interaction.

Hugging Face: AI image generation for artworks.

Replicate: Additional AI image generation capabilities.

Clarifai: Image analysis for categorization and tagging.

Twilio: SMS notifications for order tracking.

JavaMail: Email functionality for password resets.

Mapbox: Interactive geolocation for events.

iText 7: PDF handling for user report exports.

Apache PDFBox: Additional PDF generation support.

ZXing: QR code generation for cart products.

BCrypt: Password hashing for secure authentication.

JDBC: Database connectivity for MySQL interactions.
