# Restful Booker API Test Automation Framework

## Overview

This project is a REST API Test Automation Framework developed using Java, REST Assured, JUnit 5, Maven, and Allure Reporting.

The framework automates the complete CRUD (Create, Read, Update, Partial Update, Delete) lifecycle of the Restful Booker API while following industry-standard automation practices such as:

* Client-based architecture
* Request/Response model mapping
* Centralized configuration management
* Reusable request specifications
* Detailed Allure reporting
* End-to-End lifecycle validation
* Data persistence verification

---

## Tech Stack

| Technology    | Version  |
| ------------- | -------- |
| Java          | 17+      |
| Maven         | Latest   |
| REST Assured  | Latest   |
| JUnit 5       | Latest   |
| Allure Report | Latest   |
| Jackson       | Latest   |
| Lombok        | Optional |

---

## Project Structure

```text
restful-booker-tests/
│
├── pom.xml
│
├── src
│   ├── main
│   │   ├── java
│   │   │
│   │   ├── api
│   │   │   ├── clients
│   │   │   │   ├── AuthClient.java
│   │   │   │   ├── BookingClient.java
│   │   │   │   └── PingClient.java
│   │   │   │
│   │   │   └── SpecBuilder.java
│   │   │
│   │   ├── config
│   │   │   └── ConfigManager.java
│   │   │
│   │   └── models
│   │       ├── AuthRequest.java
│   │       ├── AuthResponse.java
│   │       ├── Booking.java
│   │       ├── BookingDates.java
│   │       └── BookingResponse.java
│   │
│   └── resources
│       └── application.properties
│
└── test
    └── java
        └── tests
            └── BookingCrudLifecycleTest.java
```

---

## Features Covered

### Health Check

Verifies the API service is available before executing tests.

Endpoint:

```http
GET /ping
```

---

### Authentication

Generates a valid authentication token required for secure operations.

Endpoint:

```http
POST /auth
```

---

### Create Booking

Creates a new booking and validates all response fields.

Endpoint:

```http
POST /booking
```

Validation includes:

* First Name
* Last Name
* Total Price
* Deposit Status
* Booking Dates
* Additional Needs

---

### Get Booking

Fetches the created booking and verifies complete data persistence.

Endpoint:

```http
GET /booking/{id}
```

---

### Update Booking (PUT)

Performs a complete update of the booking.

Endpoint:

```http
PUT /booking/{id}
```

Verification:

* Response validation
* Database/resource persistence validation via GET request

---

### Partial Update Booking (PATCH)

Updates selected booking fields.

Endpoint:

```http
PATCH /booking/{id}
```

Verification:

* Response validation
* Persistence validation using GET request

---

### Delete Booking

Deletes an existing booking and verifies deletion.

Endpoint:

```http
DELETE /booking/{id}
```

Verification:

* Successful deletion
* GET request returns 404 Not Found

---

## Test Execution Strategy

The tests in this framework are completely independent and can be executed in any order or in parallel.

Key Strategy Highlights:
* **Global Setup:** The auth token is generated once in `@BeforeAll` and shared statically.
* **Isolated Data Setup:** Tests requiring an existing booking dynamically create one as part of their setup, ensuring no cross-test state leakage or ordering dependencies.
* **Reusable Assertions:** Validations are standardized using helper methods to perform deep equality checks across all booking fields.

---

## Configuration

Update the application.properties file:

```properties
base.url=https://restful-booker.herokuapp.com

auth.username=admin
auth.password=password123
```

---

## Running Tests

Execute all tests:

```bash
mvn clean test
```

---

## Generating Allure Report

Generate report:

```bash
allure serve target/allure-results
```

Or:

```bash
allure generate target/allure-results --clean -o target/allure-report
allure open target/allure-report
```

---

## Key Framework Design Decisions

### Client Layer

All API interactions are encapsulated inside dedicated client classes:

* AuthClient
* BookingClient
* PingClient

This improves:

* Reusability
* Maintainability
* Readability

---

### Model Layer

Request and response payloads are mapped using Java Records.

Benefits:

* Immutable objects
* Less boilerplate code
* Cleaner serialization/deserialization

---

### Defensive Validation

The framework includes:

* Booking ID validation before API operations
* Token validation before secure operations
* Full response validation
* Persistence verification using follow-up GET requests

---

## Independent Test Flow Design

Each test case manages its own lifecycle setup and verification:

```text
[Get / Update / Patch / Delete Tests]
                 │
                 ▼
     [Setup: Create Booking]
                 │
                 ▼
      [Execute Target Action]
                 │
                 ▼
   [Verify Target Action Response]
                 │
                 ▼
   [Fetch Persistence Verification]
                 │
                 ▼
     [Assert Full Object State]
```

---

## Future Improvements

* Data-driven testing using JSON/CSV
* TestNG support
* Parallel execution
* CI/CD integration using Jenkins/GitHub Actions
* Docker execution
* Environment profiles (QA/UAT/PROD)
* Contract testing
* Schema validation
* Retry mechanism
* Logging framework integration

---

## Author

Pankaj Pilania

Automation Test Engineer | SDET

Technologies:

* Java
* REST Assured
* JUnit 5
* Selenium
* Cypress
* Playwright
* API Automation
* Allure Reporting
* Maven
* GitHub
