// First Commit - Abdalla Hashim Ahmed Abdalla - B032320119
// git commit -m "Add Course entity - Abdalla B032320119"
package com.smartcampus.courseenrolment.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String courseCode;

    @NotBlank
    private String courseName;

    @NotBlank
    private String programme;

    @Min(1)
    private int capacity;

    private int currentEnrolled;

    // Default constructor
    public Course() {}

    // Full constructor
    public Course(String courseCode, String courseName, String programme, int capacity, int currentEnrolled) {
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.programme = programme;
        this.capacity = capacity;
        this.currentEnrolled = currentEnrolled;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getProgramme() { return programme; }
    public void setProgramme(String programme) { this.programme = programme; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public int getCurrentEnrolled() { return currentEnrolled; }
    public void setCurrentEnrolled(int currentEnrolled) { this.currentEnrolled = currentEnrolled; }
}