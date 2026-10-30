# AI-Assisted Engineering Assessment – AI Usage Documentation

**AI Tool Used:** ChatGPT (GPT-6)

**Purpose:** To use AI as an engineering assistant for requirement analysis, design discussions, implementation guidance, code review and test-case identification.

---

## Prompt 1 – Requirement Analysis

**Prompt provided:**

I am working on an AI-assisted engineering assessment.

The requirement is to implement an appointment booking service that prevents overlapping active appointments for the same provider.

Analyze the requirements and:
1. Break down the implementation into tasks.
2. Identify functional requirements.
3. Identify edge cases.
4. Identify potential concurrency issues.
5. Suggest a minimal Java 17 architecture.

Do not generate the complete implementation yet. Focus on requirement understanding and design.

### AI Response Summary

ChatGPT helped me understand the core objective of the application: to ensure that a provider cannot have multiple active appointments scheduled for overlapping time intervals.

The response broke down the problem into functional requirements, edge cases, concurrency risks, and a suggested application architecture.

The following were the key takeaways from the analysis:

**1. Understanding the booking conflict rule**

The AI suggested using half-open time intervals `[startTime, endTime)` to handle appointment boundaries correctly.

For example:
- An appointment from 10:00 AM to 10:30 AM is valid.
- Another appointment from 10:30 AM to 11:00 AM is also valid.
- An appointment from 10:15 AM to 10:45 AM must be rejected because it overlaps with the existing appointment.

It also suggested the following overlap condition:

`existingStart < newEnd && existingEnd > newStart`

I found this particularly useful because it handles partial overlaps, completely contained appointments, and adjacent appointments without requiring separate conditions for each scenario.

**2. Identifying functional requirements and edge cases**

The AI identified important scenarios that need to be handled, including:
- Successful appointment booking.
- Overlapping appointments for the same provider.
- Simultaneous appointments for different providers.
- Adjacent appointments.
- Cancellation followed by rebooking.
- Cancellation of a non-existing or already-cancelled appointment.
- Missing identifiers and invalid timestamps.
- Zero or negative appointment durations.

This helped me understand that the implementation should cover more than just checking whether a time slot is available.

**3. Identifying concurrency risks**

One important observation from the AI response was the possibility of a race condition.

For example, two users might simultaneously try to book overlapping appointments with the same provider. Both requests could check availability before either appointment is saved, resulting in both bookings being accepted.

The AI explained that checking availability and saving an appointment must be treated as one atomic operation.

It also discussed database transactions and pessimistic locking as possible approaches.

**4. Suggested architecture**

The AI suggested separating the application into layers:

- Controller: Handles incoming requests.
- Service: Contains booking, cancellation and conflict-validation logic.
- Repository: Handles appointment storage and retrieval.
- Model: Represents appointment details and status.

This helped establish a clear separation between business logic and data storage.

### My Review and Engineering Decisions

After reviewing the AI response against the original assessment requirements, I identified the following decisions:

| AI suggestion | My decision | Reason |
|---|---|---|
| Use half-open intervals for appointment scheduling | Accepted | Simplifies overlap detection and allows adjacent appointments. |
| Use the proposed overlap condition | Accepted | Correctly identifies overlapping intervals without rejecting adjacent appointments. |
| Include cancellation status in conflict validation | Accepted | Cancelled appointments must not block future bookings. |
| Consider simultaneous booking requests | Accepted | Preventing race conditions is important for maintaining the booking invariant. |
| Use database transactions and pessimistic locking | Modified | The assessment explicitly permits an in-memory implementation when no starter project is supplied. I will use an approach appropriate for an in-memory store instead of introducing an unnecessary database. |
| Introduce Provider and Customer entities | Rejected for the initial scope | The assessment only requires provider identifiers and appointment details. Separate provider and customer management is outside the requested feature. |
| Add appointment retrieval and provider listing APIs | Deferred | These operations are not required by the acceptance criteria. |
| Reject appointments scheduled in the past | Not included as an assumption | The assessment does not specify this restriction, so I will avoid introducing additional business rules. |

### Additional Risks Identified During My Review

Apart from the scenarios suggested by AI, I identified some important implementation considerations:

1. **Atomicity of booking:** Checking for an existing appointment and saving a new appointment must happen atomically. Otherwise, concurrent requests could bypass the conflict check.

2. **Cancellation and booking interaction:** A cancellation occurring at the same time as a new booking should not leave the appointment store in an inconsistent state.

3. **Thread safety of the in-memory store:** Java collections such as `HashMap` are not sufficient by themselves to guarantee thread-safe booking operations. Synchronization must cover both conflict checking and saving.

4. **UTC timestamp consistency:** The assessment explicitly requires UTC. I will use Java's `Instant` rather than `LocalDateTime` to avoid ambiguity around time zones.

5. **Invalid input must not modify stored data:** Validation and conflict checks should happen before persisting a new appointment or changing an existing appointment's status.

### Verification Plan

I will independently verify the AI-assisted design decisions through:

- Unit tests for the appointment overlap condition.
- Tests for adjacent appointments and different providers.
- Tests confirming that cancelled appointments no longer block bookings.
- Tests confirming invalid requests do not modify stored appointments.
- A concurrency test in which two simultaneous overlapping booking requests are attempted for the same provider.

The concurrency test should demonstrate that only one of the competing bookings succeeds.

Actual test execution results will be recorded after implementation.

### Outcome of Prompt 1

The requirement analysis helped me establish the core booking invariant, identify the important edge cases, and understand why concurrency protection is necessary.

I will use these findings to guide the application design and implementation while keeping the solution within the scope of the assessment.

-----------------------------------------------------------------------
**Prompt 2**
Act as a senior Java 17 backend engineer.

Based on the appointment booking assessment requirements, help me design a minimal Java application.

Context:
- No starter project has been provided.
- I need to implement a standalone service using an in-memory store.
- The application should support appointment booking and cancellation.
- A provider must not have overlapping active appointments.
- All timestamps must use UTC.
- The implementation should be thread-safe.

Suggest a minimal architecture covering:

1. Appointment model and its required fields.
2. Appointment status representation.
3. Service-layer responsibilities.
4. In-memory repository design.
5. Booking and cancellation method signatures.
6. Input validation approach.
7. Thread-safety and concurrency handling.
8. Appropriate Java 17 features and collections.

Also explain:
- Why you recommend each component.
- How booking and cancellation should interact with the repository.
- How to ensure that checking availability and saving an appointment happen atomically.
- What design decisions would be unnecessary for this assessment.

Do not generate the complete implementation yet. Focus on architecture, responsibilities and design trade-offs.

## Prompt 2 – Minimal Java 17 Architecture

**My understanding of the AI response:**

Based on the second prompt, ChatGPT suggested a minimal architecture for implementing the appointment booking service without introducing unnecessary frameworks 
or infrastructure.

The proposed application consists of four main components:

1. **Appointment:** An immutable Java record containing appointment details such as ID, provider ID, customer ID, start time, end time and status.
2. **AppointmentStatus:** An enum to represent the appointment lifecycle, with two statuses: `BOOKED` and `CANCELLED`.
3. **AppointmentRepository:** An in-memory repository using a `HashMap` to store appointments and handle operations such as saving, retrieving and cancelling appointments.
4. **AppointmentService:** The service layer responsible for input validation, booking, cancellation and handling booking conflicts.

The response recommended using Java 17 features such as records, enums, UUID, `Instant`, Optional, Streams and synchronized methods.

**Important design recommendations:**

- Use `Instant` to represent timestamps consistently in UTC.
- Use an immutable appointment model to make the data easier to manage and reason about.
- Check for overlapping appointments belonging to the same provider, considering only active appointments.
- Allow appointments to be scheduled consecutively without treating adjacent time slots as overlapping.
- Retain cancelled appointments in the repository, but exclude them from future conflict checks.
- Use a synchronized repository operation to perform the availability check and appointment insertion atomically.
- Use the same synchronization mechanism for booking and cancellation to protect shared repository data.

**My review and engineering decisions:**

After reviewing the suggested architecture, I consider the following recommendations relevant to the assessment:

- **Accepted:** Plain Java 17, an in-memory `HashMap`, an immutable appointment model, `Instant` for UTC timestamps and an enum for appointment status.
- **Accepted:** Keeping booking and cancellation logic in a service layer and storage operations in a repository.
- **Accepted:** Performing conflict detection and saving within one synchronized repository operation. Synchronizing individual map operations alone would not prevent race conditions.
- **Accepted:** Using a single repository monitor rather than introducing separate locks for every provider, as the project has a limited scope.
- **Modified:** The response suggests using a customer ID, but I will include it only if it is required by the project. It is not necessary for checking provider availability.
- **Modified:** The response suggests returning the existing cancelled appointment when cancellation is requested again. I will ensure that both missing and already-cancelled appointments produce clear, documented results, as required by the project.
- **Rejected:** Additional frameworks and infrastructure such as Spring Boot, JPA, databases, Redis, Kafka and microservices, because they are unnecessary for this standalone implementation.

**Additional considerations identified:**

The proposed synchronization approach protects operations only when they share the same repository instance within one JVM. It does not provide distributed locking across multiple application instances. Since this assessment uses in-memory storage, this is an acceptable limitation.

The implementation should also validate all inputs before modifying the repository and ensure that invalid or conflicting booking requests do not change existing data.

**Outcome:**

This prompt helped me establish the application structure, identify the responsibilities of each component and understand how to handle concurrency at the repository level. I will use this design as the basis for implementation and independently verify the booking, cancellation and concurrency scenarios through unit tests.
