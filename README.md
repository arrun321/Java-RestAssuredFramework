# Java RestAssured API Test Framework

A comprehensive REST API test automation framework built with **RestAssured**, **TestNG**, **Cucumber (BDD)**, and **Allure Reports**.

---

## Tech Stack

| Tool | Version | Purpose |
|------|---------|---------|
| Java | 11 | Language |
| RestAssured | 5.4.0 | API testing library |
| TestNG | 7.9.0 | Test runner |
| Cucumber | 7.15.0 | BDD framework |
| Allure | 2.25.0 | Test reporting |
| Jackson | 2.16.1 | JSON serialization |
| Log4j2 | 2.23.0 | Logging |
| Lombok | 1.18.30 | Boilerplate reduction |
| AssertJ | 3.25.3 | Fluent assertions |
| Maven | - | Build tool |

---

## Project Structure

```
src/
├── main/java/com/restassured/framework/
│   ├── config/          # ConfigManager — loads config.properties
│   ├── constants/       # Endpoints — centralised API endpoint constants
│   ├── models/          # POJOs: User, Post, Comment, CourseItem, etc.
│   └── utils/           # RestClient, JsonUtils, TestDataFactory
│
└── test/
    ├── java/com/restassured/framework/
    │   ├── base/        # BaseTest, BaseCETest — common setup/teardown
    │   ├── tests/       # TestNG test classes (UserTests, PostTests, etc.)
    │   └── bdd/
    │       ├── runner/  # CucumberRunner
    │       ├── steps/   # Step definitions (Auth, User, Post, Course, Enrolment)
    │       ├── hooks/   # Cucumber Hooks
    │       └── context/ # TestContext for sharing state between steps
    └── resources/
        ├── features/    # Cucumber feature files
        ├── config.properties
        ├── log4j2.xml
        ├── testng.xml           # TestNG suite (Smoke + Regression)
        └── testng-cucumber.xml  # TestNG suite for BDD tests
```

---

## APIs Under Test

- **Course Enrollment API** (`https://courseenrollmentapimanagementsystem.onrender.com`) — Auth, Courses, Enrolments

---

## Setup & Prerequisites

1. **Java 11+** installed and `JAVA_HOME` set
2. **Maven 3.6+** installed
3. **Allure CLI** (optional, for serving reports locally)
   ```bash
   # Install via Scoop (Windows)
   scoop install allure
   ```

---

## Running Tests

### Run all tests
```bash
mvn clean test
```

### Run Smoke tests only
```bash
mvn clean test -Dgroups=smoke
```

### Run BDD (Cucumber) tests
```bash
mvn clean test -DsuiteXmlFile=src/test/resources/testng-cucumber.xml
```

### Run with a specific TestNG suite
```bash
mvn clean test -DsuiteXmlFile=src/test/resources/testng.xml
```

---

## Allure Reports

### Generate and open report
```bash
mvn allure:serve
```

### Generate static report
```bash
mvn allure:report
# Report output: target/site/allure-maven-plugin/index.html
```

---

## Configuration

Edit `src/test/resources/config.properties` to change environments or credentials:

```properties
environment=dev
base.url=https://jsonplaceholder.typicode.com

course.enrollment.base.url=https://courseenrollmentapimanagementsystem.onrender.com
course.enrollment.instructor.username=<username>
course.enrollment.instructor.password=<password>
```

> **Note:** Avoid committing real credentials. Use environment variables or a secrets manager for sensitive values.

---

## Logging

Logs are written to the `logs/` directory. Configuration is in `src/test/resources/log4j2.xml`.
Request/response logging can be toggled via:
```properties
log.request.response=true
```
