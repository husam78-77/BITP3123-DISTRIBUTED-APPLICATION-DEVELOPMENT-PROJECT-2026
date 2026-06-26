
package com.smartcampus.studentprofile.repository;

import com.smartcampus.studentprofile.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByMatricNumber(String matricNumber);
    Optional<Student> findByEmail(String email);
    boolean existsByMatricNumber(String matricNumber);
    boolean existsByEmail(String email);
}