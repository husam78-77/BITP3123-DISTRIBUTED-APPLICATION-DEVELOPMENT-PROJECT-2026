// First Commit - Husam Abdulatef Ahmed Yousef Harpah - B032320128
// git commit -m "Add RoomBooking REST controller - Husam B032320128"
package com.smartcampus.librarybooking.controller;

import com.smartcampus.librarybooking.entity.RoomBooking;
import com.smartcampus.librarybooking.service.LibraryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomBookingController {

    private final LibraryService libraryService;

    public RoomBookingController(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    // GET all room bookings
    @GetMapping
    public ResponseEntity<List<RoomBooking>> getAllRoomBookings() {
        return ResponseEntity.ok(libraryService.getAllRoomBookings());
    }

    // GET room bookings by student
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<RoomBooking>> getByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(libraryService.getRoomBookingsByStudent(studentId));
    }

    // POST book a room
    @PostMapping("/book")
    public ResponseEntity<RoomBooking> bookRoom(@RequestParam Long studentId,
                                                @RequestParam String roomId,
                                                @RequestParam String startTime,
                                                @RequestParam String endTime) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                libraryService.bookRoom(
                        studentId,
                        roomId,
                        LocalDateTime.parse(startTime),
                        LocalDateTime.parse(endTime)
                )
        );
    }

    // PUT cancel room booking
    @PutMapping("/cancel/{bookingId}")
    public ResponseEntity<RoomBooking> cancelBooking(@PathVariable Long bookingId) {
        return ResponseEntity.ok(libraryService.cancelRoomBooking(bookingId));
    }
}