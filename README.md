# 🏦 Bank of CLI

Bank of CLI is a terminal-based banking application built with **Java, JDBC, PostgreSQL, and Maven**. The application allows users to create and access bank accounts, manage their balance, transfer funds, and review transaction history through an interactive command-line interface.

The project follows a **layered architecture** to separate user interaction, business logic, and database operations.

---

## ✨ Features

- 🔐 **Account Registration & Login**
  - Create an account using a 4-digit PIN
  - Automatically receive a unique Account ID
  - Authenticate using an Account ID and PIN

- 💰 **Balance Management**
  - View the current account balance
  - Deposit funds
  - Withdraw funds
  - Prevent withdrawals that exceed the available balance

- 🔄 **Account Transfers**
  - Transfer funds between different accounts
  - Prevent transfers to the same account
  - Validate account existence and available funds
  - Use database transactions to ensure transfers are atomic

- 📋 **Transaction History**
  - View deposits, withdrawals, and transfers
  - Track transaction amounts and timestamps
  - View incoming transfers

- 📝 **Application Logging**
  - Records successful operations using `INFO`
  - Records application/database failures using `ERROR`
  - Uses SLF4J for application logging

- 🧪 **Testing**
  - JUnit 5 unit and integration tests
  - Mockito for Service Layer isolation
  - Positive and negative test cases
  - Tests transaction rollback and banking validation

---

## 🛠️ Tech Stack

| Technology | Purpose |
|---|---|
| Java 17 | Core application development |
| PostgreSQL | Persistent account and transaction storage |
| JDBC | Java-to-database communication |
| Maven | Dependency and build management |
| JUnit 5 | Unit and integration testing |
| Mockito | Mocking dependencies during Service Layer testing |
| SLF4J | Application logging |
| Git & GitHub | Version control |

---

## 🏗️ Architecture

Bank of CLI follows a layered architecture:

```text
┌─────────────────────────────┐
│          Main / CLI         │
│      Presentation Layer     │
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│         BankService         │
│       Business Layer        │
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│           BankDAO           │
│       Repository Layer      │
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│          PostgreSQL         │
│        Database Layer       │
└─────────────────────────────┘
```

### Presentation Layer

`Main.java` manages:

- Terminal menus
- User input
- User-facing messages
- Login sessions
- Navigation between banking operations

### Business Layer

`BankService.java` contains the application's business rules, including:

- PIN validation
- Account validation
- Positive transaction amount validation
- Insufficient-funds checks
- Self-transfer prevention

### Repository Layer

`BankDAO.java` handles communication with PostgreSQL using JDBC, including:

- Creating and finding accounts
- Authentication
- Deposits
- Withdrawals
- Transfers
- Transaction history
- Database transaction management

---

## 💳 Banking Operations

### Deposit

Deposits increase the user's balance and create a corresponding transaction record.

### Withdrawal

Withdrawals validate that sufficient funds are available before modifying the account balance.

The database query also prevents the balance from becoming negative.

### Transfer

Transfers are performed as a single database transaction:

```text
BEGIN
   │
   ├── Deduct money from sender
   │
   ├── Add money to recipient
   │
   └── Record transfer
   │
   ▼
COMMIT
```

If any part of the operation fails:

```text
ROLLBACK
```

This provides **atomicity**, preventing partial transfers where money could be removed from one account without being added to the other.

---

## 🗄️ Database

The application uses two primary PostgreSQL tables.

### Account

Stores account information including:

- Account ID
- PIN
- Balance
- Creation timestamp

### Transactions

Stores the application's transaction history including:

- Transaction ID
- Account ID
- Recipient ID
- Transaction type
- Amount
- Timestamp

Supported transaction types include:

```text
DEPOSIT
WITHDRAWAL
TRANSFER
```

---

## 🧪 Testing

The project uses **JUnit 5** for testing and includes both positive and negative test cases.

### DAO Tests

Repository tests communicate with PostgreSQL to verify actual database behavior.

Examples include:

- Account creation
- Account authentication
- Deposits updating balances
- Withdrawals updating balances
- Insufficient-funds protection
- Transfers between accounts
- Transfer rollback
- Transaction history

### Service Tests

The Service Layer is tested independently using **Mockito**.

```text
BankService
     │
     ▼
 Mock BankDAO
```

This allows business rules to be tested without requiring a database connection.

Examples include:

- Valid and invalid PIN registration
- Successful and unsuccessful login
- Invalid transaction amounts
- Insufficient funds
- Invalid accounts
- Self-transfer validation

---

## 🔒 Transaction Safety

Banking operations include several safeguards:

- Transaction amounts must be greater than zero
- Withdrawals cannot exceed the available balance
- Transfers cannot be made to the same account
- Sender and recipient accounts must exist
- Transfers use commit/rollback database transactions
- SQL queries use `PreparedStatement`
- Database errors are logged

---

## 🚀 Running the Application

### Prerequisites

Make sure the following are installed:

- Java 17+
- Maven
- PostgreSQL
- Git

### 1. Clone the Repository

```bash
git clone https://github.com/PadmeAIcaza/bank-of-cli.git
cd bank-of-cli
```

### 2. Configure PostgreSQL

Create a PostgreSQL database for the application and configure the database connection used by the project.

> Do not commit database passwords or other credentials to GitHub.

### 3. Build the Project

Using Maven:

```bash
./mvnw clean package
```

On Windows:

```bash
mvnw.cmd clean package
```

### 4. Run the Application

Run `Main.java` from your IDE or use the project's configured Maven/Java execution method.

---

## 🖥️ Example CLI

```text
                    BANK OF CLI
               Your Money, Your Command

                       $$$
                      $$$$$
                     $$$$$$$
                ╔═══════════════╗
                ║  BANK OF CLI  ║
        ╔═══════╩═══════════════╩═══════╗
        ║                               ║
        ║   ┌─────┐  ┌─────┐  ┌─────┐  ║
        ║   │     │  │     │  │     │  ║
        ║   │     │  │     │  │     │  ║
        ║   └─────┘  └─────┘  └─────┘  ║
        ║                               ║
        ║          ┌─────────┐          ║
        ║          │         │          ║
        ║          │         │          ║
        ╚══════════╧═════════╧══════════╝

              [1] Create Account
              [2] Login
              [3] Exit

              ➜ Select an option:
```

---

## 📚 What I Learned

Through this project, I gained hands-on experience with:

- Building a multilayered Java application
- Applying separation of concerns
- Connecting Java applications to PostgreSQL with JDBC
- Writing parameterized SQL queries with `PreparedStatement`
- Managing database transactions with commit and rollback
- Applying banking business rules through a Service Layer
- Implementing application logging
- Writing positive and negative tests with JUnit 5
- Using Mockito to isolate application layers
- Maintaining persistent relational data with PostgreSQL
- Applying Git version control throughout development

---

## 📁 Project Structure

```text
bank-of-cli/
│
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/bankofcli/bankofcli/
│   │           ├── Main.java
│   │           │
│   │           ├── dao/
│   │           │   └── BankDAO.java
│   │           │
│   │           ├── model/
│   │           │   ├── Account.java
│   │           │   └── Transaction.java
│   │           │
│   │           ├── service/
│   │           │   └── BankService.java
│   │           │
│   │           └── util/
│   │               └── DatabaseConnection.java
│   │
│   └── test/
│       └── java/
│           ├── AccDaoTest.java
│           └── BankServTest.java
│
├── pom.xml
├── .gitignore
└── README.md
```

---

## 👩‍💻 Author

**Padme Anadea Icaza Cocano**

Software Engineering Graduate  
University of Texas at Dallas

---

## 📄 License

This project was created for educational purposes.
