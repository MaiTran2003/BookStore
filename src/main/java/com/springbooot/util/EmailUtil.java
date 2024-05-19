package com.springbooot.util;

import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.springbooot.dto.response.ErrorResponse;
import com.springbooot.entities.User;
import com.springbooot.service.JwtService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.util.Random;

@Service
public class EmailUtil {

    private final JavaMailSender mailSender;
    private final Environment environment;
    private final JwtService jwtService;

    public EmailUtil(JavaMailSender mailSender, JwtService jwtService, Environment environment) {
        this.mailSender = mailSender;
        this.environment = environment;
        this.jwtService = jwtService;
    }

    public void sendVerificationEmail(String userEmail, User user) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(userEmail);
        message.setSubject("Email Verification");
        /**
         * Get the value of app.base-url from configuration
         */
        String baseUrl = environment.getProperty("app.base-url");
        if (baseUrl == null) {
            throw new IllegalArgumentException("'app.base-url' property is not set");
        }
        /**
         * Create verification email link using generateToken method from JwtService
         */
        String verificationToken = jwtService.generateToken(user);
        String verificationLink = baseUrl + "/verify?token=" + verificationToken;

        String emailContent = "Click the following link to verify your email: " + verificationLink;
        message.setText(emailContent);

        mailSender.send(message);
    }

    public void sendResetPasswordEmail(String email){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Set Password");

        String baseUrl = environment.getProperty("app.base-url");
        if (baseUrl == null) {
            throw new IllegalArgumentException("'app.base-url' property is not set");
        }
        String resetPasswordLink = baseUrl + "/reset-password?email=" + email;
        String emailContent = "Click the link to set your password: " + resetPasswordLink;
        message.setText(emailContent);

        mailSender.send(message);
    }

    public void sendChangePasswordEmail(String email) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);
            mimeMessageHelper.setTo(email);
            mimeMessageHelper.setSubject("Password Changed");
            mimeMessageHelper.setText("Your password has been changed successfully.");

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setMessage("Unable to send change password email. Please try again.");
        }
    }

    public void sendChangeEmail(String email, String otp) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);
            mimeMessageHelper.setTo(email);
            mimeMessageHelper.setSubject("Change Email");
            mimeMessageHelper.setText("Your OTP for changing email is: " + otp);
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setMessage("Unable to send change email. Please try again.");
        }
    }

    public String generateOtp() {
        Random random = new Random();
        int randomNumber = random.nextInt(999999);
        String output = Integer.toString(randomNumber);
        while (output.length() < 6) {
            output = "0" + output;
        }
        return output;
    }
}
