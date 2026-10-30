package com.appointment.service;

import com.appointment.exception.AppointmentConflictException;
import com.appointment.exception.AppointmentNotFoundException;
import com.appointment.exception.DuplicateAppointmentException;
import com.appointment.model.Appointment;
import com.appointment.model.AppointmentStatus;
import com.appointment.repository.AppointmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AppointmentServiceTest {

    private AppointmentRepository repository;
    private AppointmentService service;

    private final Instant start = Instant.parse("2030-01-01T10:00:00Z");
    private final Instant end = Instant.parse("2030-01-01T11:00:00Z");

    @BeforeEach
    void setUp() {
        repository = new AppointmentRepository();
        service = new AppointmentService(repository);
    }

    @Test
    void booksAppointment() {
        Appointment appointment = service.bookAppointment("provider-1", "customer-1", start, end);

        assertNotNull(appointment.id());
        assertEquals("provider-1", appointment.providerId());
        assertEquals("customer-1", appointment.customerId());
        assertEquals(AppointmentStatus.BOOKED, appointment.status());
        assertEquals(appointment, service.getAppointment(appointment.id()));
    }

    @Test
    void rejectsExactOverlap() {
        service.bookAppointment("provider-1", "customer-1", start, end);

        assertThrows(AppointmentConflictException.class,
                () -> service.bookAppointment("provider-1", "customer-2", start, end));
        assertEquals(1, service.getAppointmentsByProvider("provider-1").size());
    }

    @Test
    void rejectsPartialOverlap() {
        service.bookAppointment("provider-1", "customer-1", start, end);

        assertThrows(AppointmentConflictException.class,
                () -> service.bookAppointment("provider-1", "customer-2",
                        Instant.parse("2030-01-01T10:30:00Z"),
                        Instant.parse("2030-01-01T11:30:00Z")));
    }

    @Test
    void rejectsAppointmentContainedWithinExistingAppointment() {
        service.bookAppointment("provider-1", "customer-1", start, end);

        assertThrows(AppointmentConflictException.class,
                () -> service.bookAppointment("provider-1", "customer-2",
                        Instant.parse("2030-01-01T10:15:00Z"),
                        Instant.parse("2030-01-01T10:45:00Z")));
    }

    @Test
    void rejectsAppointmentThatContainsExistingAppointment() {
        service.bookAppointment("provider-1", "customer-1",
                Instant.parse("2030-01-01T10:15:00Z"),
                Instant.parse("2030-01-01T10:45:00Z"));

        assertThrows(AppointmentConflictException.class,
                () -> service.bookAppointment("provider-1", "customer-2", start, end));
    }

    @Test
    void allowsAdjacentAppointments() {
        service.bookAppointment("provider-1", "customer-1", start, end);

        Appointment adjacent = service.bookAppointment(
                "provider-1", "customer-2", end,
                Instant.parse("2030-01-01T12:00:00Z"));

        assertEquals(AppointmentStatus.BOOKED, adjacent.status());
        assertEquals(2, service.getAppointmentsByProvider("provider-1").size());
    }

    @Test
    void allowsSameTimeForDifferentProviders() {
        Appointment first = service.bookAppointment("provider-1", "customer-1", start, end);
        Appointment second = service.bookAppointment("provider-2", "customer-2", start, end);

        assertNotEquals(first.id(), second.id());
        assertEquals(1, service.getAppointmentsByProvider("provider-1").size());
        assertEquals(1, service.getAppointmentsByProvider("provider-2").size());
    }

    @Test
    void cancellationChangesStatusAndPreservesAppointment() {
        Appointment booked = service.bookAppointment("provider-1", "customer-1", start, end);

        Appointment cancelled = service.cancelAppointment(booked.id());

        assertEquals(AppointmentStatus.CANCELLED, cancelled.status());
        assertEquals(booked.id(), cancelled.id());
        assertEquals(start, cancelled.startTime());
        assertEquals(cancelled, service.getAppointment(booked.id()));
    }

    @Test
    void cancelledAppointmentDoesNotBlockNewBooking() {
        Appointment booked = service.bookAppointment("provider-1", "customer-1", start, end);
        service.cancelAppointment(booked.id());

        Appointment replacement = service.bookAppointment("provider-1", "customer-2", start, end);

        assertEquals(AppointmentStatus.BOOKED, replacement.status());
        assertEquals(2, service.getAppointmentsByProvider("provider-1").size());
    }

    @Test
    void repeatedCancellationIsIdempotent() {
        Appointment booked = service.bookAppointment("provider-1", "customer-1", start, end);

        Appointment firstCancellation = service.cancelAppointment(booked.id());
        Appointment secondCancellation = service.cancelAppointment(booked.id());

        assertEquals(firstCancellation, secondCancellation);
        assertEquals(AppointmentStatus.CANCELLED, secondCancellation.status());
    }

    @Test
    void missingAppointmentCannotBeCancelledOrRetrieved() {
        UUID missingId = UUID.randomUUID();

        assertThrows(AppointmentNotFoundException.class,
                () -> service.cancelAppointment(missingId));
        assertThrows(AppointmentNotFoundException.class,
                () -> service.getAppointment(missingId));
        assertTrue(service.getAppointmentsByProvider("provider-1").isEmpty());
    }

    @Test
    void rejectsInvalidBookingInputsWithoutChangingRepository() {
        assertThrows(IllegalArgumentException.class,
                () -> service.bookAppointment(" ", "customer-1", start, end));
        assertThrows(IllegalArgumentException.class,
                () -> service.bookAppointment("provider-1", null, start, end));
        assertThrows(IllegalArgumentException.class,
                () -> service.bookAppointment("provider-1", "customer-1", null, end));
        assertThrows(IllegalArgumentException.class,
                () -> service.bookAppointment("provider-1", "customer-1", start, null));
        assertThrows(IllegalArgumentException.class,
                () -> service.bookAppointment("provider-1", "customer-1", end, start));
        assertThrows(IllegalArgumentException.class,
                () -> service.bookAppointment("provider-1", "customer-1", start, start));

        assertTrue(service.getAppointmentsByProvider("provider-1").isEmpty());
    }

    @Test
    void rejectsNullAppointmentIdsForLookupAndCancellation() {
        assertThrows(IllegalArgumentException.class, () -> service.getAppointment(null));
        assertThrows(IllegalArgumentException.class, () -> service.cancelAppointment(null));
    }

    @Test
    void rejectsBlankProviderForListing() {
        assertThrows(IllegalArgumentException.class, () -> service.getAppointmentsByProvider(" "));
    }

    @Test
    void duplicateAppointmentIdIsRejectedWithoutOverwritingOriginal() {
        UUID duplicateId = UUID.randomUUID();
        Appointment first = new Appointment(
                duplicateId, "provider-1", "customer-1", start, end, AppointmentStatus.BOOKED);
        Appointment second = new Appointment(
                duplicateId, "provider-2", "customer-2", start, end, AppointmentStatus.BOOKED);

        assertTrue(repository.saveIfNoOverlap(first));
        assertThrows(DuplicateAppointmentException.class,
                () -> repository.saveIfNoOverlap(second));

        assertEquals(first, repository.findById(duplicateId).orElseThrow());
        assertEquals(List.of(first), repository.findByProviderId("provider-1"));
        assertTrue(repository.findByProviderId("provider-2").isEmpty());
    }

    @Test
    void appointmentRecordRejectsInvalidFields() {
        UUID id = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class,
                () -> new Appointment(id, " ", "customer", start, end, AppointmentStatus.BOOKED));
        assertThrows(IllegalArgumentException.class,
                () -> new Appointment(id, "provider", " ", start, end, AppointmentStatus.BOOKED));
        assertThrows(NullPointerException.class,
                () -> new Appointment(null, "provider", "customer", start, end, AppointmentStatus.BOOKED));
        assertThrows(NullPointerException.class,
                () -> new Appointment(id, "provider", "customer", null, end, AppointmentStatus.BOOKED));
        assertThrows(NullPointerException.class,
                () -> new Appointment(id, "provider", "customer", start, end, null));
    }
}
