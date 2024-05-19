package com.springbooot.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.springbooot.dto.request.BorrowBookRequest;
import com.springbooot.dto.request.ChangeEmailRequest;
import com.springbooot.dto.request.ChangePasswordRequest;
import com.springbooot.dto.request.ForgotPasswordRequest;
import com.springbooot.dto.request.ResetPasswordRequest;
import com.springbooot.dto.request.ReturnBookRequest;
import com.springbooot.dto.request.SignOutRequest;
import com.springbooot.dto.response.BorrowBookResponse;
import com.springbooot.dto.response.ChangeEmailResponse;
import com.springbooot.dto.response.ChangePasswordResponse;
import com.springbooot.dto.response.ErrorResponse;
import com.springbooot.dto.response.ForgotPasswordResponse;
import com.springbooot.dto.response.ResetPasswordResponse;
import com.springbooot.dto.response.ReturnBookResponse;
import com.springbooot.dto.response.VerificationResponse;
import com.springbooot.service.AuthenticationService;
import com.springbooot.service.BorrowingService;
import com.springbooot.service.JwtService;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final JwtService jwtService;

    private final AuthenticationService authenticationService;

    private final BorrowingService borrowingService;

    public UserController(JwtService jwtService, AuthenticationService authenticationService,
            BorrowingService borrowingService) {
        super();
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
        this.borrowingService = borrowingService;
    }

    @GetMapping()
    public ResponseEntity<String> sayHello() {
        return ResponseEntity.ok("Hi User");
    }

    @PostMapping("/logout")
    public ResponseEntity<ErrorResponse> logout(@RequestBody SignOutRequest signOutRequest,
            @RequestHeader("Authorization") String header) {
        String token = jwtService.extractTokenFromHeader(header);
        if (token == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Token not found in Authorization header."));
        }
        try {
            authenticationService.logout(signOutRequest, token);
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ErrorResponse("User " + signOutRequest.getEmail() + " Has been logged out successfully."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Logout failed! " + e.getMessage()));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        try {
            ForgotPasswordResponse forgotPasswordResponse = authenticationService.forgotPassword(forgotPasswordRequest);
            return ResponseEntity.ok(forgotPasswordResponse);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred while processing the request: " + e.getMessage()));
        }
    }

    @PutMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest) {
        if (resetPasswordRequest == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Request body is null."));
        }
        try {
            ResetPasswordResponse resetPasswordResponse = authenticationService.resetPassword(resetPasswordRequest);
            return ResponseEntity.ok(resetPasswordResponse);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred while processing the request: " + e.getMessage()));
        }
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest) {
        try {
            ChangePasswordResponse changePasswordResponse = authenticationService.changePassword(changePasswordRequest);
            return ResponseEntity.ok(changePasswordResponse);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/change-email")
    public ResponseEntity<?> changeEmail(@RequestBody ChangeEmailRequest changeEmailRequest) {
        try {
            ChangeEmailResponse changeEmailResponse = authenticationService.changeEmail(changeEmailRequest);
            return ResponseEntity.ok(changeEmailResponse);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestParam String email, @RequestParam String newEmail,
            @RequestParam String otp) {
        try {
            VerificationResponse verificationResponse = authenticationService.verifyOtp(email, newEmail, otp);

            if (verificationResponse.isSuccess()) {
                return ResponseEntity.ok(verificationResponse);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(verificationResponse);
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/borrow")
    public ResponseEntity<?> borrowBook(@RequestBody BorrowBookRequest borrowBookRequest) {
        try {
            BorrowBookResponse borrowBookResponse = borrowingService.borrowBook(borrowBookRequest);
            return ResponseEntity.ok(borrowBookResponse);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred while borrowing the book: " + e.getMessage()));
        }
    }

    @PostMapping("/return")
    public ResponseEntity<?> returnBook(@RequestBody ReturnBookRequest returnBookRequest) {
        try {
            ReturnBookResponse returnBookResponse = borrowingService.returnBook(returnBookRequest);
            return ResponseEntity.ok(returnBookResponse);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred while returning the book: " + e.getMessage()));
        }
    }

}
