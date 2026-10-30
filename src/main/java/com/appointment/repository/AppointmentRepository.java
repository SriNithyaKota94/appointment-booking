package com.appointment.repository;

import com.appointment.exception.DuplicateAppointmentException;
import com.appointment.model.Appointment;
import com.appointment.model.AppointmentStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Thread-safe, in-memory repository.
 *
 * All access to the HashMap is guarded by this repository's monitor. In particular,
 * overlap detection and insertion happen in one synchronized method.
 */
public class AppointmentRepository {

    private final Map<UUID, Appointment> appointments = new HashMap<>();

    /**
     * Saves an appointment if its ID is unique and it does not overlap an active
     * appointment for the same provider.
     *
     * @return true if saved; false if an active appointment overlaps
     * @throws DuplicateAppointmentException if the ID already exists
     */
    public synchronized boolean saveIfNoOverlap(Appointment appointment) {
        Objects.requireNonNull(appointment, "Appointment must not be null.");

        if (appointments.containsKey(appointment.id())) {
            throw new DuplicateAppointmentException(
                    "An appointment already exists with ID: " + appointment.id());
        }

        boolean hasConflict = appointments.values().stream()
                .anyMatch(existing ->
                        existing.status() == AppointmentStatus.BOOKED
                                && existing.providerId().equals(appointment.providerId())
                                && overlaps(existing, appointment));

        if (hasConflict) {
            return false;
        }

        appointments.put(appointment.id(), appointment);
        return true;
    }

    public synchronized Optional<Appointment> findById(UUID appointmentId) {
        Objects.requireNonNull(appointmentId, "Appointment ID must not be null.");
        return Optional.ofNullable(appointments.get(appointmentId));
    }

    /**
     * Marks an appointment as cancelled. Cancelling an already-cancelled
     * appointment is idempotent and returns the existing cancelled record.
     */
    public synchronized Optional<Appointment> cancel(UUID appointmentId) {
        Objects.requireNonNull(appointmentId, "Appointment ID must not be null.");

        Appointment existing = appointments.get(appointmentId);
        if (existing == null) {
            return Optional.empty();
        }

        if (existing.status() == AppointmentStatus.CANCELLED) {
            return Optional.of(existing);
        }

        Appointment cancelled = existing.withStatus(AppointmentStatus.CANCELLED);
        appointments.put(appointmentId, cancelled);
        return Optional.of(cancelled);
    }

    public synchronized List<Appointment> findByProviderId(String providerId) {
        if (providerId == null || providerId.isBlank()) {
            throw new IllegalArgumentException("Provider ID must not be null or blank.");
        }

        List<Appointment> result = new ArrayList<>();
        for (Appointment appointment : appointments.values()) {
            if (appointment.providerId().equals(providerId)) {
                result.add(appointment);
            }
        }
        return List.copyOf(result);
    }

    private boolean overlaps(Appointment first, Appointment second) {
        // Half-open intervals [start, end): touching endpoints are not overlapping.
        return first.startTime().isBefore(second.endTime())
                && first.endTime().isAfter(second.startTime());
    }
}
