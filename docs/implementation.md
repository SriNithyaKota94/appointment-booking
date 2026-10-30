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
