// First Commit - Abdalla Hashim Ahmed Abdalla - B032320119
// git commit -m "Add Course repository - Abdalla B032320119"
package com.smartcampus.courseenrolment.repository;

import com.smartcampus.courseenrolment.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByCourseCode(String courseCode);
    boolean existsByCourseCode(String courseCode);
}