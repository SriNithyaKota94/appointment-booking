package com.appointment.service;

import com.appointment.exception.AppointmentConflictException;
import com.appointment.exception.AppointmentNotFoundException;
import com.appointment.model.Appointment;
import com.appointment.model.AppointmentStatus;
import com.appointment.repository.AppointmentRepository;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Application-level booking and cancellation operations.
 * Repository methods provide the required atomicity and synchronization.
 */
public class AppointmentService {

    private final AppointmentRepository repository;

    public AppointmentService(AppointmentRepository repository) {
        this.repository = Objects.requireNonNull(repository, "Repository must not be null.");
    }

    public Appointment bookAppointment(
            String providerId,
            String customerId,
            Instant startTime,
            Instant endTime
    ) {
        validateBookingRequest(providerId, customerId, startTime, endTime);

        Appointment appointment = new Appointment(
                UUID.randomUUID(),
                providerId,
                customerId,
                startTime,
                endTime,
                AppointmentStatus.BOOKED
        );

        boolean saved = repository.saveIfNoOverlap(appointment);
        if (!saved) {
            throw new AppointmentConflictException(
                    "The provider already has an active appointment during the requested time.");
        }

        return appointment;
    }

    public Appointment cancelAppointment(UUID appointmentId) {
        if (appointmentId == null) {
            throw new IllegalArgumentException("Appointment ID must not be null.");
        }

        return repository.cancel(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException(
                        "Appointment not found: " + appointmentId));
    }

    public Appointment getAppointment(UUID appointmentId) {
        if (appointmentId == null) {
            throw new IllegalArgumentException("Appointment ID must not be null.");
        }

        return repository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException(
                        "Appointment not found: " + appointmentId));
    }

    public List<Appointment> getAppointmentsByProvider(String providerId) {
        if (providerId == null || providerId.isBlank()) {
            throw new IllegalArgumentException("Provider ID must not be null or blank.");
        }
        return repository.findByProviderId(providerId);
    }

    private void validateBookingRequest(
            String providerId,
            String customerId,
            Instant startTime,
            Instant endTime
    ) {
        if (providerId == null || providerId.isBlank()) {
            throw new IllegalArgumentException("Provider ID must not be null or blank.");
        }
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("Customer ID must not be null or blank.");
        }
        if (startTime == null) {
            throw new IllegalArgumentException("Start time must not be null.");
        }
        if (endTime == null) {
            throw new IllegalArgumentException("End time must not be null.");
        }
        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("Start time must be before end time.");
        }
    }
}
