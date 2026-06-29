
package com.smartcampus.librarybooking.controller;

import com.smartcampus.librarybooking.entity.Book;
import com.smartcampus.librarybooking.entity.BookLoan;
import com.smartcampus.librarybooking.service.LibraryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/books")
public class BookController {

    private final LibraryService libraryService;

    public BookController(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    // GET all books
    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks() {
        return ResponseEntity.ok(libraryService.getAllBooks());
    }

    // GET available books only
    @GetMapping("/available")
    public ResponseEntity<List<Book>> getAvailableBooks() {
        return ResponseEntity.ok(libraryService.getAvailableBooks());
    }

    // GET book by ID
    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        return ResponseEntity.ok(libraryService.getBookById(id));
    }

    // POST add new book
    @PostMapping
    public ResponseEntity<Book> addBook(@Valid @RequestBody Book book) {
        return ResponseEntity.status(HttpStatus.CREATED).body(libraryService.addBook(book));
    }

    // DELETE book
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBook(@PathVariable Long id) {
        libraryService.deleteBook(id);
        return ResponseEntity.ok("Book deleted successfully");
    }

    // POST borrow book
    @PostMapping("/borrow")
    public ResponseEntity<BookLoan> borrowBook(@RequestParam Long studentId,
            @RequestParam Long bookId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(libraryService.borrowBook(studentId, bookId));
    }

    // PUT return book
    @PutMapping("/return/{loanId}")
    public ResponseEntity<BookLoan> returnBook(@PathVariable Long loanId) {
        return ResponseEntity.ok(libraryService.returnBook(loanId));
    }

    // GET all loans
    @GetMapping("/loans")
    public ResponseEntity<List<BookLoan>> getAllLoans() {
        return ResponseEntity.ok(libraryService.getAllLoans());
    }

    // GET loans by student
    @GetMapping("/loans/student/{studentId}")
    public ResponseEntity<List<BookLoan>> getLoansByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(libraryService.getLoansByStudent(studentId));
    }
}