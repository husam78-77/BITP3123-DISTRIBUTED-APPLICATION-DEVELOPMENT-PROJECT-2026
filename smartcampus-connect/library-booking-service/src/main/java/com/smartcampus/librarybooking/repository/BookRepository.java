// First Commit - Abdalla Hashim Ahmed Abdalla - B032320119
// git commit -m "Add Book repository - Abdalla B032320119"
package com.smartcampus.librarybooking.repository;

import com.smartcampus.librarybooking.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {
    Optional<Book> findByIsbn(String isbn);
    List<Book> findByAvailable(boolean available);
    boolean existsByIsbn(String isbn);
}