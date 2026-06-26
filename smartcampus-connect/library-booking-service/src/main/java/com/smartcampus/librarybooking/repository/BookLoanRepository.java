// First Commit - Abdalla Hashim Ahmed Abdalla - B032320119
// git commit -m "Add BookLoan repository - Abdalla B032320119"
package com.smartcampus.librarybooking.repository;

import com.smartcampus.librarybooking.entity.BookLoan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookLoanRepository extends JpaRepository<BookLoan, Long> {
    List<BookLoan> findByStudentId(Long studentId);
    List<BookLoan> findByBookId(Long bookId);
    List<BookLoan> findByStatus(String status);
}