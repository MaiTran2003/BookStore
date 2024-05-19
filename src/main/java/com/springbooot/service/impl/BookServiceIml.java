package com.springbooot.service.impl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.springbooot.dto.request.BookRequest;
import com.springbooot.dto.response.BookResponse;
import com.springbooot.dto.response.ErrorResponse;
import com.springbooot.dto.response.SuccessResponse;
import com.springbooot.dto.response.UpdateBookResponse;
import com.springbooot.entities.Book;
import com.springbooot.repository.BookRepository;
import com.springbooot.service.BookService;

import io.jsonwebtoken.io.IOException;

@Service
public class BookServiceIml implements BookService {

    private final BookRepository bookRepository;

    public BookServiceIml(BookRepository bookRepository) {
        super();
        this.bookRepository = bookRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookResponse> searchBooks(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Specification<Book> spec = (root, query, criteriaBuilder) -> criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + keyword.toLowerCase() + "%"),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("author")), "%" + keyword.toLowerCase() + "%"),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("isbn")), "%" + keyword.toLowerCase() + "%"));

        Page<Book> bookPage = bookRepository.findAll(spec, pageable);
        List<BookResponse> bookResponses = bookPage.getContent().stream().map(this::mapToBookResponse)
                .collect(Collectors.toList());

        return new PageImpl<BookResponse>(bookResponses, pageable, bookPage.getTotalElements());
    }

    @Override
    @Transactional
    public List<BookResponse> createBooks(List<BookRequest> bookRequests) {
        List<BookResponse> createdBookResponses = new ArrayList<>();
        for (BookRequest bookRequest : bookRequests) {
            Book book = new Book();
            book.setAuthor(bookRequest.getAuthor());
            book.setIsbn(bookRequest.getIsbn());
            book.setQuantity(bookRequest.getQuantity());
            book.setTitle(bookRequest.getTitle());
            Book createdBook = bookRepository.save(book);
            createdBookResponses.add(mapToBookResponse(createdBook));
        }
        return createdBookResponses;
    }

    @Override
    @Transactional
    public UpdateBookResponse updateBook(Long id, BookRequest bookRequest) {
        var book = bookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Book not found with id:" + id));
        book.setTitle(bookRequest.getTitle());
        book.setAuthor(bookRequest.getAuthor());
        book.setIsbn(bookRequest.getIsbn());
        book.setQuantity(bookRequest.getQuantity());

        bookRepository.save(book);

        return new UpdateBookResponse("Book with id " + id + " has been successfully updated.");
    }

    @Override
    @Transactional
    public SuccessResponse deleteBook(Long id) {
        var book = bookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Book not found with id: " + id));
        bookRepository.delete(book);

        return new SuccessResponse("Book with id " + id + " has been successfully deleted.");
    }

    @Override
    @Transactional(readOnly = true)
    public BookResponse getBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Book not found with id: " + id));
        return mapToBookResponse(book);
    }

    private BookResponse mapToBookResponse(Book book) {
        BookResponse bookResponse = new BookResponse();
        bookResponse.setTitle(book.getTitle());
        bookResponse.setAuthor(book.getAuthor());
        bookResponse.setIsbn(book.getIsbn());
        bookResponse.setQuantity(book.getQuantity());
        /**
         * Map other properties as needed
         */
        return bookResponse;
    }

    @Override
    @Transactional
    public List<Book> csvToBooks(InputStream inputStream) {
        List<Book> books = new ArrayList<>();
        List<ErrorResponse> errorResponses = new ArrayList<>();

        try (BufferedReader fileReader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            CSVParser parser = new CSVParserBuilder().withSeparator(';').build();
            CSVReader csvReader = new CSVReaderBuilder(fileReader).withCSVParser(parser).build();

            String[] headers = csvReader.readNext();

            if (headers != null) {
                String[] values;
                while ((values = csvReader.readNext()) != null) {
                    Book book = new Book();
                    for (int i = 0; i < headers.length; i++) {
                        String header = headers[i].toLowerCase();
                        String value = values[i];
                        processHeader(book, header, value, errorResponses);
                    }
                    books.add(book);
                }
            } else {
                errorResponses.add(new ErrorResponse("No data found in CSV file"));
            }
        } catch (IOException e) {
            e.printStackTrace();
            errorResponses.add(new ErrorResponse("Error processing CSV file: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            errorResponses.add(new ErrorResponse("Unexpected error: " + e.getMessage()));
        }

        return books;
    }

    private void processHeader(Book book, String header, String value, List<ErrorResponse> errorResponses) {
        try {
            switch (header) {
            case "id":
                processId(book, value, errorResponses);
                break;
            case "author":
                book.setAuthor(value);
                break;
            case "isbn":
                book.setIsbn(value);
                break;
            case "quantity":
                processQuantity(book, value, errorResponses);
                break;
            case "title":
                book.setTitle(value);
                break;
            default:
                errorResponses.add(new ErrorResponse("Invalid header: " + header));
                break;
            }
        } catch (NumberFormatException e) {
            errorResponses.add(new ErrorResponse(
                    "Invalid format for " + header + ": " + value + ". Please provide a valid format."));
        } catch (Exception e) {
            errorResponses.add(new ErrorResponse("Error processing " + header + ": " + e.getMessage()));
        }
    }

    private void processId(Book book, String value, List<ErrorResponse> errorResponses) {
        if (value != null && !value.isEmpty()) {
            try {
                Long id = Long.parseLong(value);
                if (id > 0) {
                    book.setId(id);
                } else {
                    errorResponses.add(
                            new ErrorResponse("Invalid value for id: " + value + ". ID must be a positive integer."));
                }
            } catch (NumberFormatException e) {
                errorResponses.add(new ErrorResponse("Invalid value for id: " + value + ". ID must be a number."));
            }
        }
    }

    private void processQuantity(Book book, String value, List<ErrorResponse> errorResponses) {
        if (value != null && !value.isEmpty()) {
            try {
                Integer quantity = Integer.parseInt(value);
                if (quantity >= 0) {
                    book.setQuantity(quantity);
                } else {
                    errorResponses.add(new ErrorResponse(
                            "Invalid value for quantity: " + value + ". Quantity must be a non-negative integer."));
                }
            } catch (NumberFormatException e) {
                errorResponses.add(
                        new ErrorResponse("Invalid value for quantity: " + value + ". Quantity must be a number."));
            }
        }
    }

    @Override
    @Transactional
    public ErrorResponse processAndSaveData(MultipartFile file) {
        ErrorResponse errorResponse = new ErrorResponse();

        try {
            /**
             * Validate File Format and Size
             */
            String validationError = validateFile(file);
            if (validationError != null) {
                errorResponse.setMessage(validationError);
                return errorResponse;
            }
            /**
             * Process and Store Data from CSV File
             */
            List<Book> books = csvToBooks(file.getInputStream());
            if (books != null && !books.isEmpty()) {
                bookRepository.saveAll(books);
                errorResponse.setMessage("Data processed and saved successfully");
            } else {
                errorResponse.setMessage("No data found in CSV file");
            }
        } catch (IOException e) {
            e.printStackTrace();
            errorResponse.setMessage("Error processing CSV file: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            errorResponse.setMessage("Unexpected error: " + e.getMessage());
        }

        return errorResponse;
    }

    private String validateFile(MultipartFile file) {
        if (!hasCsvFormat(file)) {
            return "File is not in CSV format";
        }
        if (!hasValidSize(file)) {
            return "File size exceeds the maximum allowed limit (5MB)";
        }
        return null;
    }

    public boolean hasValidSize(MultipartFile file) {
        long maxSizeInBytes = 5 * 1024 * 1024; // 5MB
        return file.getSize() <= maxSizeInBytes;
    }

    public boolean hasCsvFormat(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        String type = "text/csv";
        if (!type.equals(file.getContentType()) && fileName != null && fileName.toLowerCase().endsWith(".csv"))
            return false;
        return true;
    }
}
