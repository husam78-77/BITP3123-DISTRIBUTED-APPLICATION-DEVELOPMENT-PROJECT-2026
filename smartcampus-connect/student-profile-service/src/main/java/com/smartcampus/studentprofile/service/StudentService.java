
package com.smartcampus.studentprofile.service;

import com.smartcampus.studentprofile.entity.Student;
import com.smartcampus.studentprofile.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    // Manual constructor injection instead of Lombok
    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public Student getStudentById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + id));
    }

    public Student getStudentByMatric(String matricNumber) {
        return studentRepository.findByMatricNumber(matricNumber)
                .orElseThrow(() -> new RuntimeException("Student not found with matric: " + matricNumber));
    }

    public Student createStudent(Student student) {
        if (studentRepository.existsByMatricNumber(student.getMatricNumber())) {
            throw new RuntimeException("Matric number already exists: " + student.getMatricNumber());
        }
        if (studentRepository.existsByEmail(student.getEmail())) {
            throw new RuntimeException("Email already exists: " + student.getEmail());
        }
        return studentRepository.save(student);
    }

    public Student updateStudent(Long id, Student updatedStudent) {
        Student existing = getStudentById(id);
        existing.setFirstName(updatedStudent.getFirstName());
        existing.setLastName(updatedStudent.getLastName());
        existing.setEmail(updatedStudent.getEmail());
        existing.setProgramme(updatedStudent.getProgramme());
        existing.setCurrentYear(updatedStudent.getCurrentYear());
        return studentRepository.save(existing);
    }

    public void deleteStudent(Long id) {
        if (!studentRepository.existsById(id)) {
            throw new RuntimeException("Student not found with id: " + id);
        }
        studentRepository.deleteById(id);
    }
}