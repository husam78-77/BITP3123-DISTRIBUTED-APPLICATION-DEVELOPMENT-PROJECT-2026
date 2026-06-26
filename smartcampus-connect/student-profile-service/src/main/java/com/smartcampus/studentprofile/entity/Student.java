
package com.smartcampus.studentprofile.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "First name is required")
    @Column(nullable = false)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Column(nullable = false)
    private String lastName;

    @Email(message = "Email must be valid")
    @NotBlank(message = "Email is required")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Matric number is required")
    @Column(nullable = false, unique = true)
    private String matricNumber;

    @NotBlank(message = "Programme is required")
    private String programme;

    @Min(1) @Max(4)
    private int currentYear;

    // Default constructor
    public Student() {}

    // All args constructor
    public Student(Long id, String firstName, String lastName, String email,
                   String matricNumber, String programme, int currentYear) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.matricNumber = matricNumber;
        this.programme = programme;
        this.currentYear = currentYear;
    }

    // Getters
    public Long getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getMatricNumber() { return matricNumber; }
    public String getProgramme() { return programme; }
    public int getCurrentYear() { return currentYear; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setEmail(String email) { this.email = email; }
    public void setMatricNumber(String matricNumber) { this.matricNumber = matricNumber; }
    public void setProgramme(String programme) { this.programme = programme; }
    public void setCurrentYear(int currentYear) { this.currentYear = currentYear; }
}