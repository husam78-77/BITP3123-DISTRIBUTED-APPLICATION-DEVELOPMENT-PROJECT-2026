// First Commit - Abdalla Hashim Ahmed Abdalla - B032320119
// git commit -m "Add RoomBooking entity - Abdalla B032320119"
package com.smartcampus.librarybooking.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "room_bookings")
public class RoomBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long studentId;

    @NotBlank
    private String roomId;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status; // CONFIRMED, CANCELLED

    // Default constructor
    public RoomBooking() {}

    // Full constructor
    public RoomBooking(Long studentId, String roomId, LocalDateTime startTime, LocalDateTime endTime, String status) {
        this.studentId = studentId;
        this.roomId = roomId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}