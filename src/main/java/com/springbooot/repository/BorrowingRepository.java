package com.springbooot.repository;

import com.springbooot.entities.Book;
import com.springbooot.entities.Borrowing;
import com.springbooot.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BorrowingRepository extends JpaRepository<Borrowing, Long> {

    Optional<Borrowing> findByUserAndBookAndReturnDateIsNull(User user, Book book);
}
