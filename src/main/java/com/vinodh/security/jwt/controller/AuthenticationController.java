package com.vinodh.security.jwt.controller;


import com.vinodh.security.jwt.dto.*;
import com.vinodh.security.jwt.model.RefreshToken;
import com.vinodh.security.jwt.model.User;
import com.vinodh.security.jwt.service.IAuthenticationService;
import com.vinodh.security.jwt.service.IRefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

    @Autowired
    private  IAuthenticationService authenticationService;

    @Autowired
    private IRefreshTokenService refreshTokenService;

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody RegisterRequest registerRequest) {
        logger.info("Register request for email={}", registerRequest.getEmail());
        User created = authenticationService.register(registerRequest);
        logger.info("User registered id={}, email={}", created.getId(), created.getEmail());
        return ResponseEntity.ok(created);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtAuthenticationResponse> login(@RequestBody LoginRequest loginRequest) {
        logger.info("Login attempt for email={}", loginRequest.getEmail());
        try {
            JwtAuthenticationResponse resp = authenticationService.login(loginRequest);
            logger.info("Login successful for email={}", loginRequest.getEmail());
            return ResponseEntity.ok(resp);
        } catch (Exception ex) {
            logger.warn("Login failed for email={} reason={}", loginRequest.getEmail(), ex.getMessage());
            throw ex;
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        logger.info("Refresh token request received");
        try {
            RefreshTokenResponse response = refreshTokenService.refreshAccessToken(request.getRefreshToken());
            logger.info("Refresh token successful for token={}", maskToken(request.getRefreshToken()));
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            logger.warn("Refresh token failed: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new RefreshTokenResponse(null));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout(@RequestBody RefreshTokenRequest request) {
        logger.info("Logout request for token={}", maskToken(request.getRefreshToken()));
        RefreshToken refreshToken = refreshTokenService.findByToken(request.getRefreshToken())
                .orElseThrow(() -> {
                    logger.warn("Logout: refresh token not found");
                    return new RuntimeException("Refresh token not found");
                });

        refreshTokenService.revokeToken(refreshToken);
        logger.info("Refresh token revoked for userId={}", refreshToken.getUser().getId());

        return ResponseEntity.ok(new LogoutResponse("Logout successful"));
    }

    private String maskToken(String token) {
        if (token == null) return "null";
        if (token.length() <= 8) return "****";
        return token.substring(0, 4) + "..." + token.substring(token.length() - 4);
    }

}