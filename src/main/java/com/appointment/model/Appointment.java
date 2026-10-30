package com.appointment.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable appointment record. The time interval is [startTime, endTime).
 */
public record Appointment(
        UUID id,
        String providerId,
        String customerId,
        Instant startTime,
        Instant endTime,
        AppointmentStatus status
) {
    public Appointment {
        Objects.requireNonNull(id, "Appointment ID must not be null.");
        providerId = requireNonBlank(providerId, "Provider ID");
        customerId = requireNonBlank(customerId, "Customer ID");
        Objects.requireNonNull(startTime, "Start time must not be null.");
        Objects.requireNonNull(endTime, "End time must not be null.");
        Objects.requireNonNull(status, "Appointment status must not be null.");

        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("Start time must be before end time.");
        }
    }

    /**
     * Returns a new appointment with the supplied status; this record is immutable.
     */
    public Appointment withStatus(AppointmentStatus newStatus) {
        return new Appointment(id, providerId, customerId, startTime, endTime, newStatus);
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be null or blank.");
        }
        return value;
    }
}
