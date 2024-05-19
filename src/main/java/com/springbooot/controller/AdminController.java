package com.springbooot.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.springbooot.dto.request.BookRequest;
import com.springbooot.dto.request.SignOutRequest;
import com.springbooot.dto.request.UserRequest;
import com.springbooot.dto.response.BookResponse;
import com.springbooot.dto.response.ErrorResponse;
import com.springbooot.dto.response.LogoutResponse;
import com.springbooot.dto.response.MessageResponse;
import com.springbooot.dto.response.SuccessResponse;
import com.springbooot.dto.response.UpdateBookResponse;
import com.springbooot.dto.response.UserResponse;
import com.springbooot.service.AuthenticationService;
import com.springbooot.service.BookService;
import com.springbooot.service.JwtService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final BookService bookService;

    private final JwtService jwtService;

    private final AuthenticationService authenticationService;

    public AdminController(BookService bookService, JwtService jwtService,
            AuthenticationService authenticationService) {
        super();
        this.bookService = bookService;
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
    }

    @GetMapping
    public ResponseEntity<String> sayHello() {
        return ResponseEntity.ok("Hi Admin");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody SignOutRequest signOutRequest,
            @RequestHeader("Authorization") String header) {
        String token = jwtService.extractTokenFromHeader(header);
        if (token == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Token not found in Authorization header."));
        }
        try {
            LogoutResponse logoutResponse = authenticationService.logout(signOutRequest, token);
            return ResponseEntity.ok(logoutResponse);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().equals("Token is invalid or null.")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Logout failed! " + e.getMessage()));
        }
    }

    @GetMapping("/search_book")
    public ResponseEntity<Page<BookResponse>> searchBooks(@RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Page<BookResponse> books = bookService.searchBooks(keyword, page, size);
        return ResponseEntity.ok(books);
    }

    @PostMapping("/create_book")
    public ResponseEntity<?> createBooks(@RequestBody List<BookRequest> bookRequests) {
        try {
            List<BookResponse> createdBooks = bookService.createBooks(bookRequests);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdBooks);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred while creating books: " + e.getMessage()));
        }
    }

    @PutMapping("/update_book/{id}")
    public ResponseEntity<?> updateBook(@PathVariable Long id, @RequestBody BookRequest bookRequest) {
        try {
            UpdateBookResponse updateBookResponse = bookService.updateBook(id, bookRequest);
            return ResponseEntity.ok(updateBookResponse);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred while updating the book: " + e.getMessage()));
        }
    }

    @DeleteMapping("/delete_book/{id}")
    public ResponseEntity<?> deleteBook(@PathVariable Long id) {
        try {
            SuccessResponse response = bookService.deleteBook(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred while deleting the book: " + e.getMessage()));
        }
    }

    @GetMapping("/get_book/{id}")
    public ResponseEntity<?> getBook(@PathVariable Long id) {
        try {
            BookResponse bookResponse = bookService.getBook(id);
            return ResponseEntity.ok(bookResponse);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred while retrieving the book: " + e.getMessage()));
        }
    }

    @PostMapping("/import")
    public ResponseEntity<MessageResponse> importBooks(@RequestParam("file") MultipartFile file) {
        if (bookService.hasCsvFormat(file) && bookService.hasValidSize(file)) {
            bookService.processAndSaveData(file);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new MessageResponse("Uploaded the file successfully: " + file.getOriginalFilename()));
        }
        return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new MessageResponse("Please upload CSV file"));
    }

    @GetMapping("/search_user")
    public ResponseEntity<Page<UserResponse>> searchUsers(@RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Page<UserResponse> users = authenticationService.searchUsers(keyword, page, size);
        return ResponseEntity.ok(users);
    }

    @PutMapping("/update_user/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserRequest userRequest) {
        try {
            UserResponse userResponse = authenticationService.updateUser(id, userRequest);
            return ResponseEntity.ok(userResponse);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred while updating the user."));
        }
    }

    @GetMapping("/get_user/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        try {
            UserResponse userResponse = authenticationService.getUser(id);
            return ResponseEntity.ok(userResponse);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred while fetching the user."));
        }
    }

    @DeleteMapping("/delete_user/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            SuccessResponse successResponse = authenticationService.deleteUser(id);
            return ResponseEntity.ok(successResponse);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred while deleting the user."));
        }
    }
}
