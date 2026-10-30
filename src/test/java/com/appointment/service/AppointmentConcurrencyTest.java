package com.appointment.service;

import com.appointment.exception.AppointmentConflictException;
import com.appointment.model.Appointment;
import com.appointment.model.AppointmentStatus;
import com.appointment.repository.AppointmentRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class AppointmentConcurrencyTest {

    @Test
    void onlyOneOfTwoConcurrentOverlappingBookingsSucceeds() throws Exception {
        AppointmentRepository repository = new AppointmentRepository();
        AppointmentService service = new AppointmentService(repository);

        Instant start = Instant.parse("2030-01-01T10:00:00Z");
        Instant end = Instant.parse("2030-01-01T11:00:00Z");

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch startSignal = new CountDownLatch(1);

        try {
            Future<Boolean> first = executor.submit(() ->
                    attemptBooking(service, "customer-1", start, end, ready, startSignal));
            Future<Boolean> second = executor.submit(() ->
                    attemptBooking(service, "customer-2", start, end, ready, startSignal));

            assertTrue(ready.await(5, TimeUnit.SECONDS), "Workers did not become ready in time.");
            startSignal.countDown();

            boolean firstSucceeded = first.get(5, TimeUnit.SECONDS);
            boolean secondSucceeded = second.get(5, TimeUnit.SECONDS);

            assertNotEquals(firstSucceeded, secondSucceeded,
                    "Exactly one concurrent booking should succeed.");

            List<Appointment> appointments = service.getAppointmentsByProvider("provider-1");
            assertEquals(1, appointments.size());
            assertEquals(AppointmentStatus.BOOKED, appointments.get(0).status());
        } finally {
            startSignal.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        }
    }

    private boolean attemptBooking(
            AppointmentService service,
            String customerId,
            Instant start,
            Instant end,
            CountDownLatch ready,
            CountDownLatch startSignal
    ) throws InterruptedException {
        ready.countDown();
        if (!startSignal.await(5, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Timed out waiting for concurrent start signal.");
        }

        try {
            service.bookAppointment("provider-1", customerId, start, end);
            return true;
        } catch (AppointmentConflictException exception) {
            return false;
        }
    }
}
