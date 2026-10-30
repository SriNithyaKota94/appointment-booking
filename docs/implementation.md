## Prompt 3 – Implementation Planning

**Purpose:**

After finalizing the basic design, I asked ChatGPT to help me break the implementation into smaller tasks. I wanted to understand which classes were needed, what each class should handle, and how booking and cancellation should work.

**Summary of the response:**

The suggested implementation uses a simple Java 17 and Maven project with separate packages for the model, repository, service, and exceptions.

The main responsibilities are:

- **Appointment:** Stores the appointment details, including its ID, provider ID, start time, end time, and status.
- **AppointmentStatus:** Keeps track of whether an appointment is booked or cancelled.
- **AppointmentRepository:** Maintains appointments in an in-memory `HashMap` and handles saving, finding, and cancelling appointments.
- **AppointmentService:** Handles booking and cancellation requests, validates inputs, and communicates with the repository.
- **Custom exceptions:** Help distinguish between booking conflicts, missing appointments, and duplicate appointment IDs.

The response also explained how to prevent overlapping bookings. Before saving a new appointment, the repository checks whether another active appointment exists for the same provider during the requested time. It uses synchronized methods so that checking and saving happen as one operation.

For cancellation, the appointment status is changed to `CANCELLED`. This means it will no longer prevent another appointment from being booked in the same time slot.

**My review and decisions:**

I found this structure suitable for the assessment because it keeps the implementation small and separates responsibilities clearly.

I decided to:
- Use Java records to keep the appointment model simple and immutable.
- Use `Instant` for timestamps so that all appointment times are handled consistently in UTC.
- Use an in-memory `HashMap` instead of introducing a database.
- Keep the booking conflict check and save operation together in the repository to avoid concurrent requests booking the same slot.
- Keep the implementation focused on booking and cancellation rather than adding unnecessary features.

I also noted a few things to be careful about during implementation:
- Customer details should only be included if they are actually required by the assessment.
- Invalid requests should be rejected before changing the stored data.
- Repeated cancellation and attempts to cancel a non-existent appointment should have clear outcomes.
- Synchronization protects access within a shared repository instance in a single application process; it does not provide coordination across multiple application instances.


**Next step:**

Use this plan to implement the model first, followed by the repository and service, and then verify the behaviour through unit and concurrency tests.
---------------------------
**Prompt 4**
**Purpose:**

Once the requirements and overall design were clear, I used ChatGPT to generate the complete Java 17 appointment booking application, including the Maven configuration, source code, and unit tests.

**Summary of the response:**

The generated project follows a simple layered structure, with separate classes for the appointment model, repository, service, and custom exceptions.

The application uses an in-memory `HashMap` to store appointments and `Instant` to represent appointment timestamps. It supports booking and cancellation while preventing overlapping active appointments for the same provider.

The implementation uses half-open time intervals to identify booking conflicts. It also uses synchronized repository methods to ensure that checking availability and saving an appointment happen atomically.

JUnit 5 test classes were included to cover booking, cancellation, overlapping appointments, invalid inputs, and concurrent booking attempts.

**My review:**

The generated project follows the planned architecture and avoids unnecessary frameworks or database dependencies. I downloaded the source code, added it to my GitHub repository, and reviewed the main implementation decisions.

One point I identified for further review is the use of `customerId` as a mandatory field. I will verify whether this is required by the original assessment before retaining it.

The generated tests were not executed in the generation environment because Maven was unavailable. I will run the build and test commands locally and record the actual results.

**Next step:**

Review the generated source code, verify that it meets all assessment requirements, execute the tests, and fix any issues identified.
