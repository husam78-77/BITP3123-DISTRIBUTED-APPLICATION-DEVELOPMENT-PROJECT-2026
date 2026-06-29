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

@Service
public class LibraryService {

    private final BookRepository bookRepository;
    private final BookLoanRepository bookLoanRepository;
    private final RoomBookingRepository roomBookingRepository;

    private final ExecutorService executorService;

    private final NotificationProducerClient notificationProducer;

    private final ConcurrentHashMap<String, ReentrantLock> roomLocks = new ConcurrentHashMap<>();

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
                LocalDate.now().plusDays(14),
                "ACTIVE");
        BookLoan saved = bookLoanRepository.save(loan);

        notificationProducer.sendAsync(
                "LIBRARY",
                "Student " + studentId + " borrowed book " + book.getTitle());

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

        notificationProducer.sendAsync(
                "LIBRARY",
                "Student " + loan.getStudentId() + " returned book " + book.getTitle());

        return loan;
    }

    public List<BookLoan> getLoansByStudent(Long studentId) {
        return bookLoanRepository.findByStudentId(studentId);
    }

    public List<BookLoan> getAllLoans() {
        return bookLoanRepository.findAll();
    }

    public Future<RoomBooking> submitBookRoom(Long studentId, String roomId,
            LocalDateTime startTime, LocalDateTime endTime) {
        Callable<RoomBooking> bookingTask = () -> bookRoom(studentId, roomId, startTime, endTime);

        return executorService.submit(bookingTask);
    }

    public RoomBooking bookRoom(Long studentId, String roomId,
            LocalDateTime startTime, LocalDateTime endTime) {

        // â”€â”€ R5: INPUT VALIDATION
        // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        // Reject invalid requests before acquiring any lock to avoid holding
        // a lock while processing clearly invalid data.
        validateBookingRequest(studentId, roomId, startTime, endTime);

        // â”€â”€ R5: PER-ROOM LOCK ACQUISITION
        // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        // ConcurrentHashMap.computeIfAbsent atomically creates a fair
        // ReentrantLock for this room on first access. Subsequent calls
        // for the same room return the existing lock instance.
        //
        // Fair lock: new ReentrantLock(true) â€” grants access in FIFO order.
        // In high-contention scenarios (e.g., popular rooms during exam season),
        // fairness prevents THREAD STARVATION where a student's request is
        // repeatedly overtaken by later arrivals.
        ReentrantLock lock = roomLocks.computeIfAbsent(roomId, key -> new ReentrantLock(true));

        lock.lock();
        try {
            // â”€â”€ R5: CRITICAL SECTION START
            // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
            // Everything inside this try block is the critical section.
            // The per-room lock guarantees that no other thread can execute
            // this code for the SAME room simultaneously, protecting the
            // shared mutable state (room_bookings table + room availability
            // schedule) from concurrent modification.
            //
            // Bookings for DIFFERENT rooms use DIFFERENT locks and therefore
            // proceed in PARALLEL â€” this is the key scalability advantage
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
                                + "Please choose a different time or room.");
            }

            // Step 3: No conflict â€” create and persist the new booking.
            RoomBooking booking = new RoomBooking(studentId, roomId, startTime, endTime, "CONFIRMED");
            RoomBooking saved = roomBookingRepository.save(booking);

            // â”€â”€ R6: ASYNCHRONOUS TCP NOTIFICATION (Producer â†’ Consumer) â”€â”€
            // Send notification after booking is confirmed, but still inside
            // the critical section so we know the booking succeeded.
            // The sendAsync call itself is non-blocking.
            notificationProducer.sendAsync(
                    "LIBRARY",
                    "Student " + studentId + " booked Room " + roomId);

            return saved;
            // â”€â”€ R5: CRITICAL SECTION END
            // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

        } finally {
            // â”€â”€ R5: LOCK RELEASE (always in finally)
            // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
            // The finally block guarantees the lock is always released, even
            // if an exception is thrown (e.g., overlap detected, DB error).
            // Failing to release the lock would cause a DEADLOCK where no
            // other thread could ever book this room again.
            lock.unlock();
        }
    }

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
                    "End time must be after start time â€” zero-duration bookings are not allowed");
        }
    }

    public RoomBooking cancelRoomBooking(Long bookingId) {
        RoomBooking booking = roomBookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Room booking not found with ID: " + bookingId));

        if ("CANCELLED".equals(booking.getStatus())) {
            throw new RuntimeException("Room booking is already cancelled");
        }

        booking.setStatus("CANCELLED");
        RoomBooking saved = roomBookingRepository.save(booking);

        notificationProducer.sendAsync(
                "LIBRARY",
                "Student " + booking.getStudentId() + " cancelled Room " + booking.getRoomId() + " booking");

        return saved;
    }

    public List<RoomBooking> getRoomBookingsByStudent(Long studentId) {
        return roomBookingRepository.findByStudentId(studentId);
    }

    public List<RoomBooking> getAllRoomBookings() {
        return roomBookingRepository.findAll();
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }
}
