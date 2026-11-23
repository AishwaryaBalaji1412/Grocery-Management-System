
# Grocery Management System

A desktop-based Grocery Management System developed using Java, Java Swing, JDBC, and MySQL. The application provides an interface for managing grocery inventory, monitoring stock levels, recording stock transactions, and generating inventory reports.

## Features

- User registration and login
- Secure password hashing using PBKDF2
- Admin and Staff user roles
- Add, update, search, and delete grocery items
- Inventory management with category filtering
- Stock-in and stock-out operations
- Automatic low-stock monitoring
- Transaction history with date and transaction-type filters
- Dashboard with inventory statistics
- Inventory valuation
- Export inventory and transaction records to CSV
- Generate and save inventory reports
- MySQL database integration
- Database transactions for stock updates

## Technologies Used

| Technology | Purpose |
|---|---|
| Java 25 | Core application development |
| Java Swing | Desktop user interface |
| MySQL | Relational database |
| JDBC | Database connectivity |
| Maven | Build and dependency management |
| JUnit 4 | Unit testing |
| PBKDF2WithHmacSHA256 | Password hashing |
| dotenv-java | Environment variable configuration |

## Project Structure

```text
grocery-management-system/
├── src/
│   ├── main/
│   │   └── java/com/grocery/
│   │       ├── App.java
│   │       ├── DatabaseConnection.java
│   │       ├── GroceryManagement.java
│   │       └── MainUI.java
│   └── test/
│       └── java/com/grocery/
│           └── AppTest.java
├── database.sql
├── .env.example
├── .gitignore
├── pom.xml
└── README.md
```

## Prerequisites

Install the following before running the application:

- Java Development Kit (JDK) 25
- Apache Maven
- MySQL Server 8.0 or later
- Git

## Setup Instructions

### 1. Clone the Repository

```bash
git clone YOUR_GITHUB_REPOSITORY_URL
cd grocery-management-system
```

### 2. Create the Database

Open MySQL and execute the database script:

```sql
SOURCE database.sql;
```

This creates the `grocery_management` database and its required tables.

### 3. Configure Environment Variables

Create a `.env` file in the project root using `.env.example` as a reference.

Configure your local MySQL credentials:

```env
DB_URL=jdbc:mysql://localhost:3306/grocery_management
DB_USERNAME=root
DB_PASSWORD=your_mysql_password
```

Use your own database username and password.

**Important:** Never commit your actual `.env` file or expose database credentials on GitHub.

### 4. Build the Project

```bash
mvn clean compile
```

### 5. Run the Application

```bash
mvn exec:java
```

## Testing

Run the automated test suite using:

```bash
mvn clean test
```

## Database Design

The application uses three primary tables:

### Users
Stores user account information, password hashes, and user roles.

### Grocery Items
Stores grocery product details, categories, quantities, prices, units, and minimum stock levels.

### Stock Transactions
Records stock-in and stock-out activities, including the associated user, quantity, notes, and transaction date.

## Security

- Passwords are hashed using PBKDF2WithHmacSHA256 with individual random salts.
- Prepared statements are used for database operations.
- Database credentials are configured through environment variables.
- Stock modifications use database transactions to maintain data consistency.

## Future Improvements

- Add advanced inventory analytics
- Expand automated test coverage
- Add more detailed reporting features

## Author

Developed as a Java desktop application project.
