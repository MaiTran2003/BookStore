package com.springbooot.service;

import org.springframework.data.domain.Page;

import com.springbooot.dto.request.ChangeEmailRequest;
import com.springbooot.dto.request.ChangePasswordRequest;
import com.springbooot.dto.request.ForgotPasswordRequest;
import com.springbooot.dto.request.RefeshTokenRequest;
import com.springbooot.dto.request.ResetPasswordRequest;
import com.springbooot.dto.request.SignInRequest;
import com.springbooot.dto.request.SignOutRequest;
import com.springbooot.dto.request.SignUpRequest;
import com.springbooot.dto.request.UserRequest;
import com.springbooot.dto.response.ChangeEmailResponse;
import com.springbooot.dto.response.ChangePasswordResponse;
import com.springbooot.dto.response.ForgotPasswordResponse;
import com.springbooot.dto.response.JwtAuthenticationResponse;
import com.springbooot.dto.response.LogoutResponse;
import com.springbooot.dto.response.ResetPasswordResponse;
import com.springbooot.dto.response.SignupResponse;
import com.springbooot.dto.response.SuccessResponse;
import com.springbooot.dto.response.UserResponse;
import com.springbooot.dto.response.VerificationResponse;

public interface AuthenticationService {

    SignupResponse signup(SignUpRequest signUpRequest);

    JwtAuthenticationResponse signin(SignInRequest signInRequest);

    JwtAuthenticationResponse refeshToken(RefeshTokenRequest refeshTokenRequest);

    LogoutResponse logout(SignOutRequest signOutRequest, String token);

    VerificationResponse verifyEmail(String verificationToken);

    ForgotPasswordResponse forgotPassword(ForgotPasswordRequest forgotPasswordRequest);

    ResetPasswordResponse resetPassword(ResetPasswordRequest resetPasswordRequest);

    ChangePasswordResponse changePassword(ChangePasswordRequest changePasswordRequest);

    ChangeEmailResponse changeEmail(ChangeEmailRequest changeEmailRequest);

    Page<UserResponse> searchUsers(String keyword, int page, int size);
    
    UserResponse updateUser(Long id, UserRequest userRequest);
    
    UserResponse getUser(Long id);
    
    SuccessResponse deleteUser(Long id);

    VerificationResponse verifyOtp(String email, String newEmail, String otp);
}
