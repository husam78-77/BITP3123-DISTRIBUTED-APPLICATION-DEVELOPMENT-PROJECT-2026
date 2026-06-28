// First Commit - Husam Abdulatef Ahmed Yousef Harpah - B032320128
// git commit -m "Add Library service logic - Husam B032320128"
package com.smartcampus.librarybooking.service;

import com.smartcampus.librarybooking.entity.Book;
import com.smartcampus.librarybooking.entity.BookLoan;
import com.smartcampus.librarybooking.entity.RoomBooking;
import com.smartcampus.librarybooking.repository.BookLoanRepository;
import com.smartcampus.librarybooking.repository.BookRepository;
import com.smartcampus.librarybooking.repository.RoomBookingRepository;
import com.smartcampus.librarybooking.socket.NotificationProducerClient;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * R5 — MULTITHREADED SERVER: FULL IMPLEMENTATION
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * This service class implements all four pillars of Requirement R5:
 *
 * <h3>1. THREAD POOL ({@link ExecutorService})</h3>
 * <ul>
 *   <li>A fixed thread pool of 10 threads is injected from
 *       {@code ThreadPoolConfig}.</li>
 *   <li>Room booking requests are submitted via {@link #submitBookRoom},
 *       which wraps the booking logic in a {@link Callable} and hands it
 *       to the pool for concurrent execution.</li>
 *   <li>This demonstrates that the server processes booking requests
 *       concurrently through a managed thread pool rather than creating
 *       a new thread for each request.</li>
 * </ul>
 *
 * <h3>2. SHARED MUTABLE STATE</h3>
 * <p>The shared mutable state consists of two interrelated resources:</p>
 * <ul>
 *   <li><b>room_bookings table</b> — the persistent store of all bookings.
 *       Multiple threads may read and write rows for the same room
 *       simultaneously.</li>
 *   <li><b>Room availability schedule</b> — the logical view derived from
 *       the table: "which time slots are free for a given room?"  This
 *       schedule changes every time a booking is inserted or cancelled.</li>
 * </ul>
 * <p>Both are <b>shared</b> (accessible by all pool threads) and
 * <b>mutable</b> (modified by insert/update operations).</p>
 *
 * <h3>3. CONCURRENCY PROTECTION ({@link ReentrantLock})</h3>
 * <ul>
 *   <li>A <b>per-room fair {@code ReentrantLock}</b> guards the critical
 *       section in {@link #bookRoom}.</li>
 *   <li>The lock makes the "check-for-overlap → insert-booking" sequence
 *       atomic for each room, eliminating the classic check-then-act
 *       race condition.</li>
 *   <li>Per-room granularity means booking Room A never blocks Room B,
 *       improving throughput under load.</li>
 * </ul>
 *
 * <h3>4. LOAD TEST ({@code RoomBookingLoadTest})</h3>
 * <ul>
 *   <li>A dedicated test utility submits 20 concurrent booking requests
 *       for the same room and verifies that exactly one succeeds.</li>
 * </ul>
 * ═══════════════════════════════════════════════════════════════════════════
 */
@Service
public class LibraryService {

    private final BookRepository bookRepository;
    private final BookLoanRepository bookLoanRepository;
    private final RoomBookingRepository roomBookingRepository;

    /**
     * R5 — THREAD POOL.
     *
     * <p>The {@link ExecutorService} bean (fixed pool of 10 threads)
     * is injected here via constructor injection.  It is used by
     * {@link #submitBookRoom} to execute booking tasks concurrently.</p>
     */
    private final ExecutorService executorService;

    /**
     * R6 — TCP Socket Producer Client.
     * Injected via constructor injection.  Used to send asynchronous
     * notification messages over TCP after library events (borrow book,
     * book room).
     */
    private final NotificationProducerClient notificationProducer;

    /**
     * R5 — PER-ROOM LOCK MAP (Concurrency Control).
     *
     * <h3>Why per-room locking instead of a single global lock?</h3>
     * <p>A single global lock serialises <b>all</b> room bookings — if
     * Student A is booking Room-101, Student B must wait even though they
     * are booking Room-202.  This creates an unnecessary bottleneck.</p>
     *
     * <p>With a {@code ConcurrentHashMap<String, ReentrantLock>}, each
     * room gets its <b>own</b> lock.  Concurrent bookings for
     * <b>different rooms proceed in parallel</b>, while bookings for
     * the <b>same room are serialised</b> — which is exactly the
     * protection we need.</p>
     *
     * <h3>Why {@code ConcurrentHashMap}?</h3>
     * <ul>
     *   <li>Thread-safe map — multiple threads can call
     *       {@code computeIfAbsent} simultaneously without corrupting
     *       the internal data structure.</li>
     *   <li>{@code computeIfAbsent} atomically creates a lock for a
     *       room on first access, avoiding the need for external
     *       synchronisation on the map itself.</li>
     * </ul>
     *
     * <h3>Why fair locks — {@code new ReentrantLock(true)}?</h3>
     * <p>A <b>fair</b> lock grants access in FIFO order.  In a
     * high-contention booking scenario (e.g. popular study rooms during
     * exam season), fairness prevents <b>thread starvation</b> — every
     * student's request is guaranteed to eventually be processed, rather
     * than being repeatedly overtaken by later arrivals.</p>
     *
     * <h3>Scalability</h3>
     * <pre>
     * Global lock throughput:  1 booking at a time (regardless of room count)
     * Per-room lock throughput: N bookings at a time (one per distinct room)
     * </pre>
     */
    private final ConcurrentHashMap<String, ReentrantLock> roomLocks = new ConcurrentHashMap<>();

    // Manual constructor injection (no Lombok, consistent with project style)
    public LibraryService(BookRepository bookRepository,
                          BookLoanRepository bookLoanRepository,
                          RoomBookingRepository roomBookingRepository,
                          ExecutorService executorService,
                          NotificationProducerClient notificationProducer) {
        this.bookRepository = bookRepository;
        this.bookLoanRepository = bookLoanRepository;
        this.roomBookingRepository = roomBookingRepository;
        this.executorService = executorService;
        this.notificationProducer = notificationProducer;
    }

    // ── Book CRUD ──────────────────────────────────────────

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public List<Book> getAvailableBooks() {
        return bookRepository.findByAvailable(true);
    }

    public Book getBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with ID: " + id));
    }

    public Book addBook(Book book) {
        if (bookRepository.existsByIsbn(book.getIsbn())) {
            throw new RuntimeException("Book with ISBN already exists: " + book.getIsbn());
        }
        book.setAvailable(true);
        return bookRepository.save(book);
    }

    public void deleteBook(Long id) {
        bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with ID: " + id));
        bookRepository.deleteById(id);
    }

    // ── Book Loans ─────────────────────────────────────────

    public BookLoan borrowBook(Long studentId, Long bookId) {
        Book book = getBookById(bookId);

        if (!book.isAvailable()) {
            throw new RuntimeException("Book is not available for borrowing");
        }

        book.setAvailable(false);
        bookRepository.save(book);

        BookLoan loan = new BookLoan(
                studentId,
                bookId,
                LocalDate.now(),
                LocalDate.now().plusDays(14), // 2-week loan period
                "ACTIVE"
        );
        BookLoan saved = bookLoanRepository.save(loan);

        // ── R6: ASYNCHRONOUS TCP NOTIFICATION (Producer → Consumer) ──────
        // Send notification to the Notification Service via TCP socket.
        // This is NON-BLOCKING — returns immediately while the TCP send
        // happens on a background thread (CompletableFuture).
        notificationProducer.sendAsync(
                "LIBRARY",
                "Student " + studentId + " borrowed book " + book.getTitle()
        );

        return saved;
    }

    public BookLoan returnBook(Long loanId) {
        BookLoan loan = bookLoanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found with ID: " + loanId));

        if ("RETURNED".equals(loan.getStatus())) {
            throw new RuntimeException("Book has already been returned");
        }

        loan.setStatus("RETURNED");
        loan.setReturnDate(LocalDate.now());
        bookLoanRepository.save(loan);

        // Mark book as available again
        Book book = getBookById(loan.getBookId());
        book.setAvailable(true);
        bookRepository.save(book);

        return loan;
    }

    public List<BookLoan> getLoansByStudent(Long studentId) {
        return bookLoanRepository.findByStudentId(studentId);
    }

    public List<BookLoan> getAllLoans() {
        return bookLoanRepository.findAll();
    }

    // ═══════════════════════════════════════════════════════════════════
    // R5 — ROOM BOOKINGS (Multithreaded Implementation)
    // ═══════════════════════════════════════════════════════════════════

    /**
     * R5 — THREAD POOL USAGE: Asynchronous Booking Submission.
     *
     * <p>Wraps the booking logic in a {@link Callable} and submits it to
     * the {@link ExecutorService} thread pool.  The caller receives a
     * {@link Future} that can be used to retrieve the result (or
     * exception) once the pooled thread completes the task.</p>
     *
     * <p>This demonstrates the <b>Thread Pool</b> requirement: booking
     * requests are not processed on the calling thread — they are
     * dispatched to one of the 10 pooled worker threads for concurrent
     * execution.</p>
     *
     * @param studentId the ID of the student making the booking
     * @param roomId    the room to book
     * @param startTime the requested start time
     * @param endTime   the requested end time
     * @return a {@link Future} containing the confirmed {@link RoomBooking},
     *         or an exception if the booking was rejected
     */
    public Future<RoomBooking> submitBookRoom(Long studentId, String roomId,
                                              LocalDateTime startTime, LocalDateTime endTime) {
        // Create a Callable task that encapsulates the booking logic
        Callable<RoomBooking> bookingTask = () -> bookRoom(studentId, roomId, startTime, endTime);

        // Submit the task to the thread pool for concurrent execution
        return executorService.submit(bookingTask);
    }

    /**
     * R5 — CONCURRENCY PROTECTION, SHARED MUTABLE STATE, and INPUT VALIDATION.
     *
     * <p>Books a study room for a student with full concurrency control
     * using per-room fair {@link ReentrantLock} instances.</p>
     *
     * <h3>SHARED MUTABLE STATE</h3>
     * <p>The <b>room_bookings table</b> and its derived <b>room availability
     * schedule</b> are the shared mutable state.  Multiple threads from the
     * {@code ExecutorService} pool may attempt to:</p>
     * <ul>
     *   <li><b>Read</b> the current bookings for a room (overlap check)</li>
     *   <li><b>Write</b> a new booking row (insert)</li>
     * </ul>
     * <p>This read-then-write pattern on shared data is a textbook source
     * of <b>race conditions</b>.</p>
     *
     * <h3>RACE CONDITION WITHOUT LOCKING</h3>
     * <pre>
     * Thread A: read bookings for Room-101  → no conflicts found
     * Thread B: read bookings for Room-101  → no conflicts found  (stale!)
     * Thread A: INSERT booking for Room-101 → SUCCESS
     * Thread B: INSERT booking for Room-101 → SUCCESS  ← DOUBLE-BOOKING!
     * </pre>
     *
     * <h3>RACE CONDITION ELIMINATED WITH PER-ROOM LOCK</h3>
     * <pre>
     * Thread A: acquire lock(Room-101) → read → no conflict → INSERT → release
     * Thread B: acquire lock(Room-101) → read → CONFLICT FOUND → reject → release
     * Thread C: acquire lock(Room-202) → executes in PARALLEL (different lock)
     * </pre>
     *
     * <h3>CRITICAL SECTION FLOW</h3>
     * <ol>
     *   <li>Validate input parameters (null checks, time-range sanity)</li>
     *   <li>Obtain the per-room lock (creates one atomically if first access)</li>
     *   <li>Acquire the lock (blocks if another thread holds it for the same room)</li>
     *   <li>Query the database for overlapping CONFIRMED bookings</li>
     *   <li>If overlap found → throw {@link RuntimeException} (booking rejected)</li>
     *   <li>If no overlap → create and save the new booking</li>
     *   <li>Release the lock in a {@code finally} block (guarantees release)</li>
     * </ol>
     *
     * @param studentId the ID of the student making the booking
     * @param roomId    the room to book (must not be null or blank)
     * @param startTime the requested start time (must not be null)
     * @param endTime   the requested end time (must be after startTime)
     * @return the confirmed {@link RoomBooking} entity
     * @throws IllegalArgumentException if any input parameter is invalid
     * @throws RuntimeException         if the room has a conflicting booking
     */
    public RoomBooking bookRoom(Long studentId, String roomId,
                                LocalDateTime startTime, LocalDateTime endTime) {

        // ── R5: INPUT VALIDATION ────────────────────────────────────────
        // Reject invalid requests before acquiring any lock to avoid holding
        // a lock while processing clearly invalid data.
        validateBookingRequest(studentId, roomId, startTime, endTime);

        // ── R5: PER-ROOM LOCK ACQUISITION ───────────────────────────────
        // ConcurrentHashMap.computeIfAbsent atomically creates a fair
        // ReentrantLock for this room on first access. Subsequent calls
        // for the same room return the existing lock instance.
        //
        // Fair lock: new ReentrantLock(true) — grants access in FIFO order.
        // In high-contention scenarios (e.g., popular rooms during exam season),
        // fairness prevents THREAD STARVATION where a student's request is
        // repeatedly overtaken by later arrivals.
        ReentrantLock lock = roomLocks.computeIfAbsent(roomId, key -> new ReentrantLock(true));

        lock.lock();
        try {
            // ── R5: CRITICAL SECTION START ──────────────────────────────
            // Everything inside this try block is the critical section.
            // The per-room lock guarantees that no other thread can execute
            // this code for the SAME room simultaneously, protecting the
            // shared mutable state (room_bookings table + room availability
            // schedule) from concurrent modification.
            //
            // Bookings for DIFFERENT rooms use DIFFERENT locks and therefore
            // proceed in PARALLEL — this is the key scalability advantage
            // of per-room locking over a single global lock.

            // Step 1: Query the database for overlapping CONFIRMED bookings.
            // The overlap condition (E_start < R_end AND E_end > R_start) is
            // implemented in RoomBookingRepository.findOverlappingBookings().
            List<RoomBooking> conflicts = roomBookingRepository
                    .findOverlappingBookings(roomId, startTime, endTime);

            // Step 2: If any overlap exists, reject the booking immediately.
            if (!conflicts.isEmpty()) {
                throw new RuntimeException(
                        "Room " + roomId + " is already booked during the requested time slot "
                        + "[" + startTime + " to " + endTime + "]. "
                        + "Found " + conflicts.size() + " conflicting booking(s). "
                        + "Please choose a different time or room."
                );
            }

            // Step 3: No conflict — create and persist the new booking.
            RoomBooking booking = new RoomBooking(studentId, roomId, startTime, endTime, "CONFIRMED");
            RoomBooking saved = roomBookingRepository.save(booking);

            // ── R6: ASYNCHRONOUS TCP NOTIFICATION (Producer → Consumer) ──
            // Send notification after booking is confirmed, but still inside
            // the critical section so we know the booking succeeded.
            // The sendAsync call itself is non-blocking.
            notificationProducer.sendAsync(
                    "LIBRARY",
                    "Student " + studentId + " booked Room " + roomId
            );

            return saved;
            // ── R5: CRITICAL SECTION END ────────────────────────────────

        } finally {
            // ── R5: LOCK RELEASE (always in finally) ────────────────────
            // The finally block guarantees the lock is always released, even
            // if an exception is thrown (e.g., overlap detected, DB error).
            // Failing to release the lock would cause a DEADLOCK where no
            // other thread could ever book this room again.
            lock.unlock();
        }
    }

    /**
     * Validates input parameters for a room booking request.
     *
     * <p>Validation is performed <b>before</b> acquiring any lock so that
     * clearly invalid requests do not waste lock-holding time.</p>
     *
     * @param studentId the student ID (must not be null)
     * @param roomId    the room ID (must not be null or blank)
     * @param startTime the booking start time (must not be null)
     * @param endTime   the booking end time (must be strictly after startTime)
     * @throws IllegalArgumentException if any validation rule is violated
     */
    private void validateBookingRequest(Long studentId, String roomId,
                                        LocalDateTime startTime, LocalDateTime endTime) {
        if (studentId == null) {
            throw new IllegalArgumentException("Student ID must not be null");
        }
        if (roomId == null || roomId.isBlank()) {
            throw new IllegalArgumentException("Room ID must not be null or blank");
        }
        if (startTime == null) {
            throw new IllegalArgumentException("Start time must not be null");
        }
        if (endTime == null) {
            throw new IllegalArgumentException("End time must not be null");
        }
        if (endTime.isBefore(startTime)) {
            throw new IllegalArgumentException(
                    "End time (" + endTime + ") must not be before start time (" + startTime + ")");
        }
        if (endTime.isEqual(startTime)) {
            throw new IllegalArgumentException(
                    "End time must be after start time — zero-duration bookings are not allowed");
        }
    }

    public RoomBooking cancelRoomBooking(Long bookingId) {
        RoomBooking booking = roomBookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Room booking not found with ID: " + bookingId));

        if ("CANCELLED".equals(booking.getStatus())) {
            throw new RuntimeException("Room booking is already cancelled");
        }

        booking.setStatus("CANCELLED");
        return roomBookingRepository.save(booking);
    }

    public List<RoomBooking> getRoomBookingsByStudent(Long studentId) {
        return roomBookingRepository.findByStudentId(studentId);
    }

    public List<RoomBooking> getAllRoomBookings() {
        return roomBookingRepository.findAll();
    }

    /**
     * Exposes the managed {@link ExecutorService} for use by load-test
     * utilities that need to submit concurrent tasks to the same thread pool.
     *
     * @return the shared {@link ExecutorService} singleton bean
     */
    public ExecutorService getExecutorService() {
        return executorService;
    }
}