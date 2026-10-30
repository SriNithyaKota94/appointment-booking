# Appointment Booking Service

A simple Java-based appointment booking service that prevents overlapping active appointments for the same provider.

## Overview

This project implements the core booking and cancellation functionality using Java 17 and an in-memory repository.

The main objective is to ensure that a provider cannot have two active appointments scheduled during overlapping time slots.

## Features

- Book an appointment for a provider.
- Prevent overlapping active appointments for the same provider.
- Allow appointments for different providers at the same time.
- Allow adjacent appointments without treating them as conflicts.
- Cancel an existing appointment.
- Allow a cancelled time slot to be booked again.
- Validate appointment details and reject invalid requests.
- Protect booking operations against concurrent overlapping requests.

## Technology Stack

- Java 17
- Maven
- JUnit 5
- Java Time API
- Java Concurrency utilities

## Project Structure

```text
src/
├── main/java/com/appointment/
│   ├── Main.java
│   ├── model/
│   ├── repository/
│   ├── service/
│   └── exception/
└── test/java/com/appointment/
    └── service/
```

## How to Run

Make sure Java and Maven are installed.

Compile the project:

```bash
mvn clean compile
```

Run the tests:

```bash
mvn test
```

Run the demonstration:

```bash
mvn exec:java
```

## Test Results

The test suite was executed using Maven in GitHub Codespaces.

- Total tests: 17
- Passed: 17
- Failures: 0
- Errors: 0
- Skipped: 0

See [Testing Documentation](docs/TESTING.md) for details.

## Design and Implementation

- [Design](docs/DESIGN.md)
- [Implementation](docs/IMPLEMENTATION.md)
- [Testing](docs/TESTING.md)
- [AI Usage](docs/AI_USAGE.md)

## Scope and Limitations

- Uses an in-memory data store; appointments are not persisted after application restart.
- Synchronization protects operations within a shared repository instance in a single JVM.
- No REST API, database, authentication, or user interface is included.

These choices keep the implementation focused on the appointment booking requirements.
