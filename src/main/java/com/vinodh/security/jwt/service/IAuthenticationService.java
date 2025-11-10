package com.vinodh.security.jwt.service;

import com.vinodh.security.jwt.model.User;
import com.vinodh.security.jwt.dto.JwtAuthenticationResponse;
import com.vinodh.security.jwt.dto.LoginRequest;
import com.vinodh.security.jwt.dto.RegisterRequest;

public interface IAuthenticationService {
    User register(RegisterRequest registerRequest);
    public JwtAuthenticationResponse login(LoginRequest loginRequest);
}
