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

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenServiceImpl implements IRefreshTokenService {

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

            // if expired → regenerate
            if (token.getExpiryDate().isBefore(Instant.now())) {
                token.setToken(UUID.randomUUID().toString());
                token.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
                token.setRevoked(false);
            }

            return refreshTokenRepository.save(token);
        }

        // else create new one
        RefreshToken newToken = new RefreshToken();
        newToken.setUser(user);
        newToken.setToken(UUID.randomUUID().toString());
        newToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        newToken.setRevoked(false);
        return refreshTokenRepository.save(newToken);
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Override
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.isRevoked()) throw new RuntimeException("Token revoked. Login again.");
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Token expired. Login again.");
        }
        return token;
    }

    @Override
    public void revokeToken(RefreshToken token) {
        token.setRevoked(true);
        refreshTokenRepository.save(token);
    }

    public RefreshTokenResponse refreshAccessToken(String refreshTokenStr) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenStr)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        // Verify expiration
        if (refreshToken.isRevoked() || refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("Refresh token expired or revoked. Login again.");
        }
        User user = refreshToken.getUser();
        String newAccessToken = jwtService.generateToken(user);
        return new RefreshTokenResponse(newAccessToken);
    }


}
