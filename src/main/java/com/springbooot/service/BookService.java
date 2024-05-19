package com.springbooot.service;

import java.io.InputStream;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.springbooot.dto.request.BookRequest;
import com.springbooot.dto.response.BookResponse;
import com.springbooot.dto.response.ErrorResponse;
import com.springbooot.dto.response.SuccessResponse;
import com.springbooot.dto.response.UpdateBookResponse;
import com.springbooot.entities.Book;

@Service
public interface BookService {

    Page<BookResponse> searchBooks(String keyword, int page, int size);

    List<BookResponse> createBooks(List<BookRequest> bookRequests);

    UpdateBookResponse updateBook(Long id, BookRequest bookRequest);

    SuccessResponse deleteBook(Long id);

    ErrorResponse processAndSaveData(MultipartFile file);

    List<Book> csvToBooks(InputStream inputStream);
    
    BookResponse getBook(Long id);

    boolean hasValidSize(MultipartFile file);

    boolean hasCsvFormat(MultipartFile file);
}
