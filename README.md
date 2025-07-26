Dorm8

⚠️ This project is currently IN DEVELOPMENT and not production-ready.

Dorm8 is a Java backend application for managing shared expenses within groups.

Features:
- User and group management
- Tracking group members' balances
- Creating and handling expense requests
- CRUD operations with JDBC and SQL database
- Basic session and request handling

Technologies:
- Java 17
- JDBC
- MySQL (or compatible SQL database)
- Maven
- JUnit tests

Getting Started:
1. Clone the repo:
   git clone https://github.com/FFryczek/Dorm8.git

2. Configure your database and update connection settings in DataSourceConnectionHelper.

3. Build the project with Maven:
   mvn clean install

4. Run the application (or tests) from your IDE or command line.

Usage:
- The backend exposes methods to manage users, groups, expenses, and requests.
- See tests for example usage of services and DAOs.

Tests:
Run tests with:
   mvn test

Made by Filip Fryczek
