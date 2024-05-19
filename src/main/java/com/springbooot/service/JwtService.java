package com.springbooot.service;

import java.util.Map;
import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;

import com.springbooot.entities.User;

public interface JwtService {

    String extractUserName(String token);

    String generateToken(UserDetails userDetails);

    boolean isTokenValid(String token, UserDetails userDetails);

    boolean isTokenExpired(String token);

    String generateRefeshToken(Map<String, Object> extraClaims, UserDetails userDetails);

    boolean isTokenLoggedOut(String username, String token);

    Optional<User> findByVerificationToken(String verificationToken);

    String extractTokenFromHeader(String header);
}
