// First Commit - Abdalla Hashim Ahmed Abdalla - B032320119
// git commit -m "Add Enrolment repository - Abdalla B032320119"
package com.smartcampus.courseenrolment.repository;

import com.smartcampus.courseenrolment.entity.Enrolment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrolmentRepository extends JpaRepository<Enrolment, Long> {
    List<Enrolment> findByStudentId(Long studentId);
    List<Enrolment> findByCourseId(Long courseId);
    Optional<Enrolment> findByStudentIdAndCourseIdAndSemester(Long studentId, Long courseId, String semester);
    List<Enrolment> findByStatus(String status);
}