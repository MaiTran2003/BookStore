package com.springbooot.service.impl;

import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.springbooot.entities.User;
import com.springbooot.repository.UserRepository;
import com.springbooot.service.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtServiceImpl implements JwtService {

    private final UserRepository userRepository;

    public JwtServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public String generateToken(UserDetails userDetails) {
        return Jwts.builder().setSubject(userDetails.getUsername()).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 60 * 60 * 1000))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256).compact();
    }

    @Override
    @Transactional
    public String generateRefeshToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder().setClaims(extraClaims).setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 604800000))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256).compact();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode("dHJhbnRoZW5uZ29jbWFpbmd1eWVudGhpbmdvY3BodW5nMTIzNDU2Nzg5");
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extract username from JWT
     */
    @Override
    @Transactional(readOnly = true)
    public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract any claim from JWT
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolvers) {
        final Claims claims = extractAllClaims(token);
        return claimsResolvers.apply(claims);
    }

    /**
     * Extract all claims from token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
    }

    /**
     * Check token validity
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String userName = extractUserName(token);
        return (userName.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * Check if token is expired
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    /**
     * Check if token is logged out
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isTokenLoggedOut(String username, String token) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Set<String> loggedOutTokens = user.getLoggedOutTokens();
        if (loggedOutTokens == null) {
            return false;
        }

        return loggedOutTokens.contains(token);
    }

    /**
     * Find user by verification token
     */
    @Override
    @Transactional
    public Optional<User> findByVerificationToken(String verificationToken) {
        return userRepository.findAll().stream().filter(user -> verificationToken.equals(user.getVerificationToken()))
                .findFirst();
    }

    /**
     * Extract token from header
     */
    @Override
    @Transactional(readOnly = true)
    public String extractTokenFromHeader(String header) {
        if (StringUtils.isNotEmpty(header) && StringUtils.startsWith(header, "Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
