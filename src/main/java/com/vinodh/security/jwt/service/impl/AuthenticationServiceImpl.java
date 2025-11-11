package com.vinodh.security.jwt.service.impl;

import com.vinodh.security.jwt.dto.JwtAuthenticationResponse;
import com.vinodh.security.jwt.dto.LoginRequest;
import com.vinodh.security.jwt.dto.RegisterRequest;
import com.vinodh.security.jwt.model.RefreshToken;
import com.vinodh.security.jwt.model.Role;
import com.vinodh.security.jwt.model.User;
import com.vinodh.security.jwt.repository.IUserRepository;
import com.vinodh.security.jwt.service.IAuthenticationService;
import com.vinodh.security.jwt.service.IJwtService;
import com.vinodh.security.jwt.service.IRefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AuthenticationServiceImpl implements IAuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

    @Autowired
    private  IUserRepository userRepository;
    @Autowired
    private  PasswordEncoder passwordEncoder;
    @Autowired
    private  AuthenticationManager authenticationManager;
    @Autowired
    private  IJwtService jwtService;
    @Autowired
    private IRefreshTokenService refreshTokenService;

    public User register(RegisterRequest registerRequest) {
        logger.info("Registering new user email={}", registerRequest.getEmail());
        User user = new User();

        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(Role.USER);

        User saved = userRepository.save(user);
        logger.info("User registration completed id={}, email={}", saved.getId(), saved.getEmail());
        return saved;
    }

    public JwtAuthenticationResponse login(LoginRequest loginRequest) {
        logger.info("Authenticating user email={}", loginRequest.getEmail());
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(),
                loginRequest.getPassword())
        );

        var user = userRepository.findByEmail(loginRequest.getEmail()).orElseThrow(() -> {
            logger.warn("Authentication failed: user not found email={}", loginRequest.getEmail());
            return new IllegalArgumentException("Invalid username or password");
        });

        // Check if user is disabled
        if (user.getIsDisabled()) {  // or user.isActive() depending on your field
            logger.warn("Authentication blocked: user disabled email={}", loginRequest.getEmail());
            throw new RuntimeException("User account is disabled. Please contact support.");
        }

        var jwt = jwtService.generateToken(user);
        logger.debug("Access token generated for user email={}", loginRequest.getEmail());

        JwtAuthenticationResponse jwtAuthenticationResponse = new JwtAuthenticationResponse();
        jwtAuthenticationResponse.setAccessToken(jwt);

        RefreshToken refreshToken = refreshTokenService.createOrReuseRefreshToken(user);
        jwtAuthenticationResponse.setRefreshToken(refreshToken.getToken());
        logger.info("Refresh token issued for user id={}", user.getId());

        return jwtAuthenticationResponse;
    }

}
