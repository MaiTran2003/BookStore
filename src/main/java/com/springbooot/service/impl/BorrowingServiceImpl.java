package com.springbooot.service.impl;

import java.sql.Date;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.springbooot.dto.request.BorrowBookRequest;
import com.springbooot.dto.request.ReturnBookRequest;
import com.springbooot.dto.response.BorrowBookResponse;
import com.springbooot.dto.response.ReturnBookResponse;
import com.springbooot.entities.Book;
import com.springbooot.entities.Borrowing;
import com.springbooot.repository.BookRepository;
import com.springbooot.repository.BorrowingRepository;
import com.springbooot.repository.UserRepository;
import com.springbooot.service.BorrowingService;

@Service
public class BorrowingServiceImpl implements BorrowingService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final BorrowingRepository borrowingRepository;

    public BorrowingServiceImpl(BookRepository bookRepository, UserRepository userRepository,
            BorrowingRepository borrowingRepository) {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.borrowingRepository = borrowingRepository;
    }

    @Override
    @Transactional
    public BorrowBookResponse borrowBook(BorrowBookRequest borrowBookRequest) {
        Long bookId = borrowBookRequest.getBookId();
        Long userId = borrowBookRequest.getUserId();
        /**
         * Check if the book exists
         */
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found with id: " + bookId));
        /**
         * Check if the user exists
         */
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        /**
         * Check if the user exists
         */
        if (book.getQuantity() <= 0) {
            throw new IllegalArgumentException("The book is out of stock.");
        }
        /**
         * Check if the user has already borrowed this book
         */
        Optional<Borrowing> existingBorrowing = borrowingRepository.findByUserAndBookAndReturnDateIsNull(user, book);
        if (existingBorrowing.isPresent()) {
            throw new IllegalArgumentException("You have already borrowed this book.");
        }
        /**
         * Decrease the quantity of the book
         */
        book.setQuantity(book.getQuantity() - 1);
        bookRepository.save(book);
        /**
         * Create a new borrowing record
         */
        Borrowing borrowing = new Borrowing();
        borrowing.setUser(user);
        borrowing.setBook(book);
        borrowing.setBorrowDate(new Date(System.currentTimeMillis()));
        borrowingRepository.save(borrowing);
        /**
         * Create a response object
         */
        BorrowBookResponse borrowBookResponse = new BorrowBookResponse();
        borrowBookResponse.setMessage("Book borrowed successfully");
        borrowBookResponse.setBookId(bookId);
        borrowBookResponse.setUserId(userId);

        return borrowBookResponse;
    }

    @Override
    @Transactional
    public ReturnBookResponse returnBook(ReturnBookRequest returnBookRequest) {
        ReturnBookResponse returnBookResponse = new ReturnBookResponse();
        Long borrowingId = returnBookRequest.getBorrowingId();

        Borrowing borrowing = borrowingRepository.findById(borrowingId).orElse(null);

        if (borrowing == null) {
            throw new IllegalArgumentException("Borrowing not found with id: " + borrowingId);
        }

        if (borrowing.getReturnDate() != null) {
            throw new IllegalArgumentException("This book has already been returned.");
        }

        Book book = borrowing.getBook();
        book.setQuantity(book.getQuantity() + 1);
        bookRepository.save(book);

        borrowing.setReturnDate(new Date(System.currentTimeMillis()));
        borrowingRepository.save(borrowing);

        returnBookResponse.setMessage("Book returned successfully");

        return returnBookResponse;
    }
}
