package com.vinodh.security.jwt.service.impl;

import com.vinodh.security.jwt.service.IJwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import java.security.Key;
import java.util.Date;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class JwtServiceImpl implements IJwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtServiceImpl.class);

    private final Key SECRET_KEY = getSigning();

    // generating token
    public String generateToken(UserDetails userDetails) {
        String token = Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 24))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
        logger.debug("Generated JWT for user={}, expiresAt={}", userDetails.getUsername(), getExpirationDateFromToken(token));
        return token;
    }

    // extracting userName
    public String extractUserName(String token) {
        return extractClaims(token,Claims::getSubject);
    }

    // extraction claims
    public <T> T extractClaims(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // signing key
    private Key getSigning() {
        return Keys.secretKeyFor(SignatureAlgorithm.HS256);
    }

    // checking expiration
    public Date getExpirationDateFromToken(String token) {
        return extractClaims(token, Claims::getExpiration);
    }

    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    // validation token
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUserName(token);
        boolean valid = (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        if (!valid) {
            logger.debug("Token validation failed for user={}, expired={}", username, isTokenExpired(token));
        } else {
            logger.debug("Token validated for user={}", username);
        }
        return valid;
    }

}
