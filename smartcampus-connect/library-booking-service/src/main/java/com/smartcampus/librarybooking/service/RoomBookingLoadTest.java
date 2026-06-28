// R5 - Multithreaded Server: Load Test Utility
// git commit -m "Add RoomBookingLoadTest for R5 concurrency demo - Husam B032320128"
package com.smartcampus.librarybooking.service;

import com.smartcampus.librarybooking.entity.RoomBooking;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * R5 — LOAD TEST UTILITY (Concurrency Demonstration)
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * This utility class verifies the R5 Multithreaded Server implementation by
 * simulating 20 concurrent booking requests for the <b>SAME room and time
 * slot</b>.
 *
 * <h3>What It Proves</h3>
 * <ol>
 *   <li><b>Thread Pool</b> — All 20 requests are submitted to the
 *       {@link ExecutorService} (fixed pool of 10 threads), proving that
 *       the server uses a managed thread pool for concurrent processing.</li>
 *   <li><b>Shared Mutable State</b> — All 20 threads attempt to modify the
 *       same shared resource (the {@code room_bookings} table for a single
 *       room), creating a high-contention scenario.</li>
 *   <li><b>Concurrency Protection</b> — The per-room {@code ReentrantLock}
 *       in {@code LibraryService.bookRoom()} ensures that exactly
 *       <b>ONE</b> of the 20 requests succeeds.  The other 19 are rejected
 *       with a conflict error, proving that the lock prevents double-bookings
 *       even under heavy concurrent load.</li>
 * </ol>
 *
 * <h3>Expected Output</h3>
 * <pre>
 *   Exactly  1 line : "SUCCESS: Student X booked room LIB-ROOM-A1"
 *   Exactly 19 lines: "REJECTED: Student Y — conflict detected"
 *   Summary         : "1 succeeded, 19 rejected (out of 20 total)"
 * </pre>
 *
 * <h3>How to Run</h3>
 * <p>This is <b>NOT</b> a {@code CommandLineRunner} — it does not execute
 * automatically at application startup.  Instead, call
 * {@link #execute(LibraryService)} from:</p>
 * <ul>
 *   <li>A JUnit/integration test (recommended for CI)</li>
 *   <li>A REST endpoint for manual trigger</li>
 *   <li>A {@code main()} method for standalone execution</li>
 * </ul>
 * ═══════════════════════════════════════════════════════════════════════════
 */
public class RoomBookingLoadTest {

    /** Number of concurrent booking requests to simulate. */
    private static final int TOTAL_REQUESTS = 20;

    /** The room all students will attempt to book simultaneously. */
    private static final String TARGET_ROOM = "LIB-ROOM-A1";

    /** Booking time slot — all requests target the same window. */
    private static final LocalDateTime START_TIME = LocalDateTime.of(2026, 6, 15, 10, 0);
    private static final LocalDateTime END_TIME   = LocalDateTime.of(2026, 6, 15, 12, 0);

    // Private constructor — utility class, not instantiated
    private RoomBookingLoadTest() {}

    /**
     * Executes the R5 load test against the provided {@link LibraryService}.
     *
     * <p>Submits {@value #TOTAL_REQUESTS} concurrent booking requests for
     * room {@value #TARGET_ROOM} during the same time slot.  Uses the
     * managed {@link ExecutorService} thread pool (10 threads) injected
     * into the service.</p>
     *
     * <h3>Metrics Reported</h3>
     * <ul>
     *   <li>Total requests submitted</li>
     *   <li>Successful bookings (should be exactly 1)</li>
     *   <li>Rejected bookings (should be exactly 19)</li>
     *   <li>Total execution time (milliseconds)</li>
     *   <li>Throughput (requests per second)</li>
     * </ul>
     *
     * @param libraryService the service instance to test (must be fully
     *                       initialised with its {@code ExecutorService} and
     *                       repository dependencies)
     */
    public static void execute(LibraryService libraryService) {

        System.out.println();
        System.out.println("=====================================================================");
        System.out.println("  R5 - ROOM BOOKING LOAD TEST (" + TOTAL_REQUESTS + " Concurrent Requests)");
        System.out.println("=====================================================================");
        System.out.println();
        System.out.println("  Target Room        : " + TARGET_ROOM);
        System.out.println("  Time Slot          : " + START_TIME + " to " + END_TIME);
        System.out.println("  Concurrent Requests: " + TOTAL_REQUESTS);
        System.out.println("  Thread Pool Size   : 10 (fixed)");
        System.out.println("  Lock Type          : ReentrantLock(fair=true), per-room granularity");
        System.out.println();
        System.out.println("---------------------------------------------------------------------");
        System.out.println("  INDIVIDUAL RESULTS");
        System.out.println("---------------------------------------------------------------------");

        // ── R5 — THREAD POOL USAGE ──────────────────────────────────────
        // Obtain the same ExecutorService bean used by the production code.
        // All 20 tasks are submitted to this pool, which has 10 threads —
        // so up to 10 booking attempts run truly in parallel.
        ExecutorService executor = libraryService.getExecutorService();
        List<Future<RoomBooking>> futures = new ArrayList<>();

        // Record start time for throughput calculation
        long startNanos = System.nanoTime();

        // ── SUBMIT 20 CONCURRENT TASKS ──────────────────────────────────
        for (int i = 0; i < TOTAL_REQUESTS; i++) {
            long studentId = 1001L + i; // Students 1001 through 1020

            Callable<RoomBooking> bookingTask = () ->
                    libraryService.bookRoom(studentId, TARGET_ROOM, START_TIME, END_TIME);

            futures.add(executor.submit(bookingTask));
        }

        // ── COLLECT RESULTS ─────────────────────────────────────────────
        int successCount = 0;
        int rejectedCount = 0;

        for (int i = 0; i < futures.size(); i++) {
            long studentId = 1001L + i;
            try {
                // Future.get() blocks until the task completes.
                // If the task threw an exception, get() re-throws it
                // wrapped in an ExecutionException.
                RoomBooking result = futures.get(i).get();
                successCount++;
                System.out.println("  [OK]     Student " + studentId
                        + " -> BOOKED (ID: " + result.getId() + ")");
            } catch (Exception e) {
                rejectedCount++;
                String reason = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                System.out.println("  [REJECT] Student " + studentId + " -> " + reason);
            }
        }

        // ── COMPUTE METRICS ─────────────────────────────────────────────
        long elapsedNanos = System.nanoTime() - startNanos;
        double elapsedMs = elapsedNanos / 1_000_000.0;
        double throughput = (TOTAL_REQUESTS / elapsedMs) * 1000.0; // requests/sec

        // ── PRINT SUMMARY ───────────────────────────────────────────────
        System.out.println();
        System.out.println("---------------------------------------------------------------------");
        System.out.println("  LOAD TEST SUMMARY");
        System.out.println("---------------------------------------------------------------------");
        System.out.println("  Total Requests  : " + TOTAL_REQUESTS);
        System.out.println("  Succeeded       : " + successCount);
        System.out.println("  Rejected        : " + rejectedCount);
        System.out.println(String.format("  Execution Time  : %.2f ms", elapsedMs));
        System.out.println(String.format("  Throughput      : %.2f requests/sec", throughput));
        System.out.println();

        // ── PASS/FAIL VERDICT ───────────────────────────────────────────
        if (successCount == 1 && rejectedCount == TOTAL_REQUESTS - 1) {
            System.out.println("  RESULT: PASS");
            System.out.println("    -> Only 1 booking accepted out of " + TOTAL_REQUESTS + " concurrent requests.");
            System.out.println("    -> All " + rejectedCount + " conflicting requests were correctly rejected.");
            System.out.println("    -> The per-room ReentrantLock successfully prevented double-bookings.");
        } else if (successCount > 1) {
            System.out.println("  RESULT: FAIL");
            System.out.println("    -> " + successCount + " bookings were accepted (expected exactly 1).");
            System.out.println("    -> This indicates a concurrency control defect.");
        } else {
            System.out.println("  RESULT: UNEXPECTED");
            System.out.println("    -> No bookings succeeded (expected exactly 1).");
        }

        System.out.println();
        System.out.println("=====================================================================");
        System.out.println("  R5 - LOAD TEST COMPLETE");
        System.out.println("=====================================================================");
        System.out.println();
    }
}
