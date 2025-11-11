package com.vinodh.security.jwt.service.impl;

import com.vinodh.security.jwt.dto.RefreshTokenResponse;
import com.vinodh.security.jwt.model.RefreshToken;
import com.vinodh.security.jwt.model.User;
import com.vinodh.security.jwt.repository.IRefreshTokenRepository;
import com.vinodh.security.jwt.repository.IUserRepository;
import com.vinodh.security.jwt.service.IRefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenServiceImpl implements IRefreshTokenService {

    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenServiceImpl.class);

    @Value("${app.jwtRefreshExpirationMs}")
    private Long refreshTokenDurationMs;

    @Autowired
    private IRefreshTokenRepository refreshTokenRepository;

    @Autowired
    private JwtServiceImpl jwtService;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private UserServiceImpl userDetailsService;


    @Override
    public RefreshToken createOrReuseRefreshToken(User user) {
        Optional<RefreshToken> existing = refreshTokenRepository.findByUser(user);
        if (existing.isPresent()) {
            RefreshToken token = existing.get();
            logger.debug("Found existing refresh token for userId={} tokenMasked={}", user.getId(), mask(token.getToken()));

            // if expired → regenerate
            if (token.getExpiryDate().isBefore(Instant.now())) {
                token.setToken(UUID.randomUUID().toString());
                token.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
                token.setRevoked(false);
                logger.info("Refresh token expired; regenerated for userId={}", user.getId());
            } else {
                logger.debug("Reusing existing refresh token for userId={}", user.getId());
            }

            return refreshTokenRepository.save(token);
        }

        // else create new one
        RefreshToken newToken = new RefreshToken();
        newToken.setUser(user);
        newToken.setToken(UUID.randomUUID().toString());
        newToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        newToken.setRevoked(false);
        RefreshToken saved = refreshTokenRepository.save(newToken);
        logger.info("Created new refresh token for userId={} tokenMasked={}", user.getId(), mask(saved.getToken()));
        return saved;
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        logger.debug("Lookup refresh token tokenMasked={}", mask(token));
        return refreshTokenRepository.findByToken(token);
    }

    @Override
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.isRevoked()) {
            logger.warn("Refresh token revoked for userId={}", token.getUser().getId());
            throw new RuntimeException("Token revoked. Login again.");
        }
        if (token.getExpiryDate().isBefore(Instant.now())) {
            logger.warn("Refresh token expired for userId={}", token.getUser().getId());
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Token expired. Login again.");
        }
        logger.debug("Refresh token verified for userId={}", token.getUser().getId());
        return token;
    }

    @Override
    public void revokeToken(RefreshToken token) {
        token.setRevoked(true);
        refreshTokenRepository.save(token);
        logger.info("Refresh token revoked for userId={} tokenMasked={}", token.getUser().getId(), mask(token.getToken()));
    }

    public RefreshTokenResponse refreshAccessToken(String refreshTokenStr) {
        logger.info("Refresh access token requested tokenMasked={}", mask(refreshTokenStr));
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenStr)
                .orElseThrow(() -> {
                    logger.warn("Refresh token not found tokenMasked={}", mask(refreshTokenStr));
                    return new RuntimeException("Refresh token not found");
                });

        // Verify expiration
        if (refreshToken.isRevoked() || refreshToken.getExpiryDate().isBefore(Instant.now())) {
            logger.warn("Refresh token expired or revoked for userId={}", refreshToken.getUser().getId());
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("Refresh token expired or revoked. Login again.");
        }
        User user = refreshToken.getUser();
        String newAccessToken = jwtService.generateToken(user);
        logger.info("New access token generated for userId={}", user.getId());
        return new RefreshTokenResponse(newAccessToken);
    }

    private String mask(String token) {
        if (token == null) return "null";
        if (token.length() <= 8) return "****";
        return token.substring(0,4) + "..." + token.substring(token.length()-4);
    }


}
