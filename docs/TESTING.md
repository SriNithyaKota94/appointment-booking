# Testing Documentation

## 1. Overview

The appointment booking service is tested using JUnit 5 and Maven.

The main objective of testing is to verify that appointments are booked and cancelled correctly, overlapping appointments are prevented, and concurrent booking requests do not result in duplicate time-slot allocations.

Both normal scenarios and invalid input scenarios are covered.

## 2. Test Environment

- **Language:** Java
- **Java target version:** 17
- **Build tool:** Maven
- **Testing framework:** JUnit 5
- **Storage:** In-memory repository

## 3. Test Classes

The project contains two test classes:

### AppointmentServiceTest

Tests the functional behaviour of the appointment service, including booking, cancellation, validation, and conflict detection.

### AppointmentConcurrencyTest

Tests concurrent booking requests to verify that two customers cannot successfully book overlapping appointments with the same provider at the same time.

## 4. Test Scenarios

### 4.1 Booking Tests

| Scenario | Expected Result |
|---|---|
| Book an appointment with valid details | Appointment is created successfully |
| Book an appointment with the same provider and exact same time slot | Booking is rejected |
| Book an appointment with partially overlapping time | Booking is rejected |
| Book an appointment contained within an existing appointment | Booking is rejected |
| Book an appointment that contains an existing appointment | Booking is rejected |
| Book an appointment immediately after an existing appointment ends | Booking is allowed |
| Book an appointment for a different provider at the same time | Booking is allowed |

### 4.2 Cancellation Tests

| Scenario | Expected Result |
|---|---|
| Cancel an existing appointment | Appointment status changes to CANCELLED |
| Book a previously occupied time slot after cancellation | Booking is allowed |
| Cancel an already cancelled appointment | Cancellation remains idempotent |
| Cancel an appointment that does not exist | Appropriate exception is returned |

### 4.3 Validation Tests

| Scenario | Expected Result |
|---|---|
| Missing provider ID | Request is rejected |
| Missing customer ID | Request is rejected |
| Missing start or end timestamp | Request is rejected |
| Start time equal to end time | Request is rejected |
| End time before start time | Request is rejected |
| Duplicate appointment ID | Duplicate appointment exception is thrown |

### 4.4 Concurrency Test

The concurrency test uses two worker threads to submit overlapping booking requests for the same provider.

Both threads are released using `CountDownLatch` so that their booking attempts begin close together.

The test verifies that:

- Exactly one booking succeeds.
- The other booking is rejected due to an appointment conflict.
- Only one active appointment is stored for the provider.

This verifies the atomic check-and-save behaviour implemented using synchronization in the repository.

## 5. How to Run the Tests

Run the following command from the project root directory:

```bash
mvn clean test
```

This command cleans the previous build output, compiles the source and test code, and executes the automated test suite.

Alternatively, run:

```bash
mvn test
```

## 6. Test Execution Results

The test suite was executed successfully in GitHub Codespaces.

**Command executed:**

```bash
mvn test
```

**Execution summary:**

| Test Class | Tests Run | Failures | Errors | Skipped |
|---|---:|---:|---:|---:|
| AppointmentServiceTest | 16 | 0 | 0 | 0 |
| AppointmentConcurrencyTest | 1 | 0 | 0 | 0 |
| **Total** | **17** | **0** | **0** | **0** |

**Final result:** BUILD SUCCESS

All 17 executed tests passed.

## 7. Limitations

- The concurrency test covers simultaneous overlapping booking attempts using a shared repository instance.
- The tests use an in-memory repository and do not cover persistence or recovery after application restart.
- The concurrency test does not simulate multiple application instances or distributed transactions.

## 8. Conclusion

The automated tests verify the core appointment booking and cancellation requirements, including overlap prevention, input validation, and concurrent booking protection.

The current test suite completed successfully with zero failures and zero errors.
