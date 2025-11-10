package com.vinodh.security.jwt.service;

import com.vinodh.security.jwt.dto.RefreshTokenResponse;
import com.vinodh.security.jwt.model.RefreshToken;
import com.vinodh.security.jwt.model.User;

import java.util.Optional;

public interface IRefreshTokenService {
    RefreshToken createOrReuseRefreshToken(User user);
    Optional<RefreshToken> findByToken(String token);
    RefreshToken verifyExpiration(RefreshToken token);
    void revokeToken(RefreshToken token);
    RefreshTokenResponse refreshAccessToken(String refreshTokenStr);
}
