package com.appointment;

import com.appointment.exception.AppointmentConflictException;
import com.appointment.model.Appointment;
import com.appointment.repository.AppointmentRepository;
import com.appointment.service.AppointmentService;

import java.time.Instant;

/**
 * Small command-line demonstration; no user interface or external service is required.
 */
public class Main {

    public static void main(String[] args) {
        AppointmentRepository repository = new AppointmentRepository();
        AppointmentService service = new AppointmentService(repository);

        Appointment booked = service.bookAppointment(
                "provider-101",
                "customer-501",
                Instant.parse("2026-11-02T10:00:00Z"),
                Instant.parse("2026-11-02T10:30:00Z")
        );

        System.out.println("Booked: " + booked);

        try {
            service.bookAppointment(
                    "provider-101",
                    "customer-502",
                    Instant.parse("2026-11-02T10:15:00Z"),
                    Instant.parse("2026-11-02T10:45:00Z")
            );
        } catch (AppointmentConflictException exception) {
            System.out.println("Booking rejected: " + exception.getMessage());
        }

        Appointment cancelled = service.cancelAppointment(booked.id());
        System.out.println("Cancelled: " + cancelled);

        Appointment replacement = service.bookAppointment(
                "provider-101",
                "customer-502",
                Instant.parse("2026-11-02T10:15:00Z"),
                Instant.parse("2026-11-02T10:45:00Z")
        );
        System.out.println("Replacement booking: " + replacement);
    }
}
