package com.springbooot.service.impl;

import java.util.HashMap;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.springbooot.entities.Role;
import com.springbooot.entities.User;
import com.springbooot.repository.UserRepository;
import com.springbooot.service.AuthenticationService;
import com.springbooot.service.JwtService;
import com.springbooot.util.EmailUtil;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EmailUtil emailUtil;

    public AuthenticationServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager, JwtService jwtService, EmailUtil emailUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.emailUtil = emailUtil;
    }

    @Override
    @Transactional
    public SignupResponse signup(SignUpRequest signUpRequest) {
        /**
         * Check if the email is already registered
         */
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new IllegalArgumentException("Email has already been registered!");
        }
        /**
         * Validate email and password format
         */
        if (!isValidEmail(signUpRequest.getEmail()) || !isValidPassword(signUpRequest.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password format!");
        }
        /**
         * Create a new user and set information from SignUpRequest
         */
        User user = new User();
        user.setEmail(signUpRequest.getEmail());
        user.setFirstname(signUpRequest.getFirstname());
        user.setLastname(signUpRequest.getLastname());
        user.setRole(Role.USER);
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        /**
         * Encrypt password and generate verification token
         */
        String verificationToken = jwtService.generateToken(user);
        user.setVerificationToken(verificationToken);

        User savedUser = userRepository.save(user);
        /**
         * Send verification email
         */
        emailUtil.sendVerificationEmail(user.getEmail(), user);
        /**
         * Map the saved user to UserResponse and set in the response
         */
        UserResponse userResponse = mapToUserResponse(savedUser);
        SignupResponse signupResponse = new SignupResponse();
        signupResponse.setUserResponse(userResponse);

        return new SignupResponse("You have successfully registered.");
    }

    @Override
    @Transactional
    public JwtAuthenticationResponse signin(SignInRequest signInRequest) {

        var user = userRepository.findByEmail(signInRequest.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Email does not exist! "));

        if (!user.isVerified()) {
            throw new IllegalArgumentException("Email has not been verified!");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(signInRequest.getEmail(), signInRequest.getPassword()));
        } catch (AuthenticationException e) {
            throw new IllegalArgumentException("Invalid email or password!");
        }
        var jwt = jwtService.generateToken(user);
        var refeshToken = jwtService.generateRefeshToken(new HashMap<>(), user);

        JwtAuthenticationResponse jwtAuthenticationResponse = new JwtAuthenticationResponse();

        jwtAuthenticationResponse.setToken(jwt);
        jwtAuthenticationResponse.setRefeshToken(refeshToken);
        return jwtAuthenticationResponse;
    }

    @Override
    @Transactional
    public VerificationResponse verifyEmail(String verificationToken) {
        VerificationResponse response = new VerificationResponse();

        var user = userRepository.findByVerificationToken(verificationToken)
                .orElseThrow(() -> new IllegalArgumentException("Invalid verification token!"));

        if (jwtService.isTokenValid(verificationToken, user) && !user.isVerified()) {
            user.setVerified(true);
            user.setVerificationToken(null);
            userRepository.save(user);

            response.setMessage("Email verification successful");
            response.setSuccess(true);
        } else {
            throw new IllegalArgumentException("Invalid verification token!");
        }

        return response;
    }

    @Override
    @Transactional
    public LogoutResponse logout(SignOutRequest signOutRequest, String token) {
        String userEmail = signOutRequest.getEmail();
        String password = signOutRequest.getPassword();
        /**
         * Find the user by email
         */
        var user = userRepository.findByEmail(signOutRequest.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Email does not exist!"));
        /**
         * Validate the password
         */
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        /**
         * Check if the token is null or empty
         */
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token is invalid or null.");
        }
        /**
         * Check if the token is valid
         */
        if (!jwtService.isTokenValid(token, user)) {
            throw new IllegalArgumentException("Invalid token.");
        }
        /**
         * Check if the token has been logged out before adding it to the list
         */
        if (!jwtService.isTokenLoggedOut(userEmail, token)) {
            user.getLoggedOutTokens().add(token);
            user.getLoggedOutTokens().removeIf(t -> t.equals(jwtService.extractTokenFromHeader(token)));
            userRepository.save(user);
            return new LogoutResponse(userEmail + " has been logged out successfully.");
        } else {
            throw new IllegalArgumentException("Token has already been logged out.");
        }
    }

    @Override
    @Transactional
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest forgotPasswordRequest) {
        String email = forgotPasswordRequest.getEmail();
        String message;

        emailUtil.sendResetPasswordEmail(email);
        message = "Reset password email has been sent successfully.";

        /**
         * Create ForgotPasswordResponse with message and email
         */
        ForgotPasswordResponse forgotPasswordResponse = new ForgotPasswordResponse();
        forgotPasswordResponse.setMessage(message);
        forgotPasswordResponse.setEmail(email);

        return forgotPasswordResponse;
    }

    @Override
    @Transactional
    public ResetPasswordResponse resetPassword(ResetPasswordRequest resetPasswordRequest) {
        String email = resetPasswordRequest.getEmail();
        String newPassword = resetPasswordRequest.getNewPassword();

        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format: ");
        }

        if (!isValidPassword(newPassword)) {
            throw new IllegalArgumentException("Invalid password format. Password must meet certain criteria.");
        }

        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Email does not exist!"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return new ResetPasswordResponse("The password has been successfully reset for the user: " + email);
    }

    @Override
    @Transactional
    public JwtAuthenticationResponse refeshToken(RefeshTokenRequest refeshTokenRequest) {
        String userEmail = jwtService.extractUserName(refeshTokenRequest.getToken());
        var user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found for the provided token"));

        if (jwtService.isTokenValid(refeshTokenRequest.getToken(), user)) {
            String jwt = jwtService.generateToken(user);

            JwtAuthenticationResponse jwtAuthenticationResponse = new JwtAuthenticationResponse();
            jwtAuthenticationResponse.setToken(jwt);
            jwtAuthenticationResponse.setRefeshToken(refeshTokenRequest.getToken());
            return jwtAuthenticationResponse;
        } else {
            throw new IllegalArgumentException("Invalid refresh token");
        }
    }

    @Override
    @Transactional
    public ChangePasswordResponse changePassword(ChangePasswordRequest changePasswordRequest) {
        String email = changePasswordRequest.getEmail();
        String oldPassword = changePasswordRequest.getOldPassword();
        String newPassword = changePasswordRequest.getNewPassword();

        var user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Email does not exist !"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Incorrect old password.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        emailUtil.sendChangePasswordEmail(user.getEmail());

        return new ChangePasswordResponse("Password has been successfully changed for user: " + email);
    }

    @Override
    @Transactional
    public ChangeEmailResponse changeEmail(ChangeEmailRequest changeEmailRequest) {
        String oldEmail = changeEmailRequest.getOldEmail();
        var user = userRepository.findByEmail(oldEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + oldEmail));

        String otp = emailUtil.generateOtp();
        user.setOtp(otp);
        userRepository.save(user);

        emailUtil.sendChangeEmail(oldEmail, otp);

        return new ChangeEmailResponse("An OTP has been sent to your old email address to confirm the email change.");
    }

    @Override
    @Transactional
    public VerificationResponse verifyOtp(String email, String newEmail, String otp) {

        VerificationResponse response = new VerificationResponse();
        try {
            var user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));

            if (user.getOtp() != null && otp.equals(user.getOtp())) {
                user.setEmail(newEmail);
                user.setOtp(null);
                userRepository.save(user);

                response.setMessage("Email verification successful");
                response.setSuccess(true);
            } else {
                throw new IllegalArgumentException("Invalid or expired OTP");
            }
        } catch (IllegalArgumentException e) {
            response.setMessage(e.getMessage());
            response.setSuccess(false);
        } catch (Exception e) {
            String errorMessage = "Verification failed! " + e.getMessage();
            response.setMessage(errorMessage);
            response.setSuccess(false);
        }
        return response;
    }

    @Override
    @Transactional
    public Page<UserResponse> searchUsers(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Specification<User> spec = (root, query, criteriaBuilder) -> criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(root.get("firstname")), "%" + keyword.toLowerCase() + "%"),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("lastname")), "%" + keyword.toLowerCase() + "%"),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), "%" + keyword.toLowerCase() + "%"));

        Page<User> userPage = userRepository.findAll(spec, pageable);
        return userPage.map(this::mapToUserResponse);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserRequest userRequest) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        user.setEmail(userRequest.getEmail());
        user.setFirstname(userRequest.getFirstname());
        user.setLastname(userRequest.getLastname());
        user.setRole(userRequest.getRole());
        User updatedUser = userRepository.save(user);
        return mapToUserResponse(updatedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        return mapToUserResponse(user);
    }

    @Override
    @Transactional
    public SuccessResponse deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        userRepository.delete(user);
        return new SuccessResponse("User with id " + id + " has been successfully deleted.");
    }

    private UserResponse mapToUserResponse(User user) {
        UserResponse userResponse = new UserResponse();
        userResponse.setEmail(user.getEmail());
        userResponse.setFirstname(user.getFirstname());
        userResponse.setLastname(user.getLastname());
        userResponse.setRole(user.getRole());
        return userResponse;
    }

    public boolean isValidEmail(String email) {
        return email != null && email.contains("@") && email.contains(".");
    }

    public boolean isValidPassword(String password) {
        return password != null && password.length() >= 4;
    }

}
