## Prompt 3 – Implementation Planning

**Purpose:**

After finalising the architecture, I used ChatGPT to break the implementation into smaller tasks and understand the responsibilities of each component.

### Summary of the Response

The suggested project structure included separate packages for the model, repository, service and custom exceptions.

The main responsibilities were:

- **Appointment:** Represents appointment details using a Java record.
- **AppointmentStatus:** Maintains the appointment status (`BOOKED` or `CANCELLED`).
- **AppointmentRepository:** Stores appointments in an in-memory `HashMap` and handles data operations.
- **AppointmentService:** Handles booking, cancellation and input validation.
- **Custom exceptions:** Handle booking conflicts, missing appointments and duplicate IDs.

The response also explained how synchronized repository methods can prevent concurrent requests from booking overlapping slots. Cancelled appointments are retained but excluded from future conflict checks.

### My Review and Decisions

I found this approach suitable for the assessment because it keeps the application simple and responsibilities clearly separated.

I decided to:
- Use Java records for an immutable appointment model.
- Use `Instant` for UTC timestamps.
- Use an in-memory `HashMap` instead of a database.
- Keep the availability check and save operation together to prevent race conditions.
- Avoid adding features that are not part of the assessment.

I also kept input validation, repeated cancellation behaviour and thread-safety limitations in mind while planning the implementation.

### Outcome

This planning helped me understand the implementation sequence and what each class was expected to handle before generating the actual code.

---

## Prompt 4 – Implementation and Code Review

**Purpose:**

Once the requirements and design were clear, I used ChatGPT to generate the Java 17 application, including the Maven configuration, source code and JUnit 5 test cases.

### Summary of the Response

The generated project followed the planned architecture and included:

- An immutable appointment model and status enum.
- An in-memory `HashMap` repository.
- A service layer for booking and cancellation.
- Custom exceptions for different failure scenarios.
- JUnit 5 tests covering booking, cancellation, validation, overlapping appointments and concurrent booking attempts.

The implementation used half-open intervals to detect overlaps and synchronized repository methods to protect shared data.

### My Review and Decisions

After reviewing the generated code, I noticed that the overlap-checking logic was implemented inside the repository rather than entirely in the service layer.

I questioned this design because I initially expected business logic to reside in the service layer.

After discussing it with ChatGPT, I understood the reason behind this approach: the availability check and save operation need to happen atomically. If they are separate operations, two threads could simultaneously find a slot available and both book it.

Keeping these operations inside the same synchronized repository method ensures that one thread completes the check and save before another thread can proceed.

I reviewed this trade-off and decided to retain the approach because it provides the required thread safety without introducing additional complexity.

I also reviewed the use of `customerId` as a mandatory field and identified it for verification against the original requirements.

The generated tests were not executed during code generation because Maven was unavailable in that environment. I planned to run the build and tests locally rather than assume they had passed.

### Outcome and Next Steps

I downloaded the generated project, reviewed the implementation and added the source code to my GitHub repository.

My next steps are to verify the implementation against the original requirements, execute the Maven build and test suite, and address any issues identified during testing.
