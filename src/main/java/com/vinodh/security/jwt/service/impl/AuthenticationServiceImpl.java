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

@Service
public class AuthenticationServiceImpl implements IAuthenticationService {

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
        User user = new User();

        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(Role.USER);

        return userRepository.save(user);
    }

    public JwtAuthenticationResponse login(LoginRequest loginRequest) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(),
                loginRequest.getPassword())
        );

        var user = userRepository.findByEmail(loginRequest.getEmail()).orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        // Check if user is disabled
        if (user.getIsDisabled()) {  // or user.isActive() depending on your field
            throw new RuntimeException("User account is disabled. Please contact support.");
        }

        var jwt = jwtService.generateToken(user);

        JwtAuthenticationResponse jwtAuthenticationResponse = new JwtAuthenticationResponse();
        jwtAuthenticationResponse.setAccessToken(jwt);

        RefreshToken refreshToken = refreshTokenService.createOrReuseRefreshToken(user);
        jwtAuthenticationResponse.setRefreshToken(refreshToken.getToken());

        return jwtAuthenticationResponse;
    }

}
