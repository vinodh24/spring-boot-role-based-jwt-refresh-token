package com.vinodh.security.jwt.service;

import com.vinodh.security.jwt.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Optional;

public interface IUserService {

    UserDetailsService userDetailsService();
    User create(User user);
    List<User> getAll();
    Optional<User> getById(Long id);
    User update(Long id, User user);
    void delete(Long id);
    Optional<User> getByEmail(String email);

}
