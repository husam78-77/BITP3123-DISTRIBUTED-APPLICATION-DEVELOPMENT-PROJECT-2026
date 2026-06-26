// First Commit - Ahmed Abdulrahman Ahmed Ali Gamel - B032320114
// git commit -m "Add Notification entity - Ahmed B032320114"
package com.smartcampus.notification.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;        // ENROLMENT, LIBRARY, PAYMENT
    private String message;
    private String status;      // RECEIVED, PROCESSED
    private LocalDateTime receivedAt;

    // Default constructor
    public Notification() {}

    // Full constructor
    public Notification(String type, String message, String status, LocalDateTime receivedAt) {
        this.type = type;
        this.message = message;
        this.status = status;
        this.receivedAt = receivedAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getReceivedAt() { return receivedAt; }
    public void setReceivedAt(LocalDateTime receivedAt) { this.receivedAt = receivedAt; }
}