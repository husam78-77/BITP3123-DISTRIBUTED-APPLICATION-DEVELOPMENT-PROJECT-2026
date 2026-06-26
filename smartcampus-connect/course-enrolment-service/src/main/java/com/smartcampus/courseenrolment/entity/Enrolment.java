// First Commit - Abdalla Hashim Ahmed Abdalla - B032320119
// git commit -m "Add Enrolment entity - Abdalla B032320119"
package com.smartcampus.courseenrolment.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "enrolments")
public class Enrolment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long studentId;

    @NotNull
    private Long courseId;

    @NotBlank
    private String semester;

    private String status; // ENROLLED, DROPPED

    // Default constructor
    public Enrolment() {}

    // Full constructor
    public Enrolment(Long studentId, Long courseId, String semester, String status) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.semester = semester;
        this.status = status;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}