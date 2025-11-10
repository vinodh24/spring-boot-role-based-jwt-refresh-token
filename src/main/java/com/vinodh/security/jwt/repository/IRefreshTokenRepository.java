package com.vinodh.security.jwt.repository;

import com.vinodh.security.jwt.model.RefreshToken;
import com.vinodh.security.jwt.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface IRefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByUser(User user);
    Optional<RefreshToken> findByToken(String token);
    List<RefreshToken> findAllByUser(User user);
    List<RefreshToken> findAllByExpiryDateBefore(Instant now);
}
