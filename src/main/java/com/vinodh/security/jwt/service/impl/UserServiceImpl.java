package com.vinodh.security.jwt.service.impl;

import com.vinodh.security.jwt.model.User;
import com.vinodh.security.jwt.repository.IUserRepository;
import com.vinodh.security.jwt.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements IUserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetailsService userDetailsService() {
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                logger.debug("Loading user by username={}", username);
                return userRepository.findByEmail(username)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            }
        };
    }

    // CRUD operations for admin use

    @Override
    public User create(User user) {
        logger.info("Creating user email={}", user.getEmail());
        user.setId(null);
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        User saved = userRepository.save(user);
        logger.info("User created id={}, email={}", saved.getId(), saved.getEmail());
        return saved;
    }

    @Override
    public List<User> getAll() {
        logger.debug("Fetching all users");
        return userRepository.findAll();
    }

    @Override
    public Optional<User> getById(Long id) {
        logger.debug("Fetching user by id={}", id);
        return userRepository.findById(id.intValue());
    }

    @Override
    public User update(Long id, User payload) {
        logger.info("Updating user id={}", id);
        User existing = userRepository.findById(id.intValue()).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (payload.getFirstName() != null) existing.setFirstName(payload.getFirstName());
        if (payload.getLastName() != null) existing.setLastName(payload.getLastName());
        if (payload.getEmail() != null) existing.setEmail(payload.getEmail());
        if (payload.getRole() != null) existing.setRole(payload.getRole());
        return userRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        logger.info("Deleting user id={}", id);
        userRepository.deleteById(id.intValue());
    }

    @Override
    public Optional<User> getByEmail(String email) {
        logger.debug("Fetching user by email={}", email);
        return userRepository.findByEmail(email);
    }

    private boolean isPasswordEncoded(String password) {
        // bcrypt hashes look like: $2a$, $2b$, or $2y$ followed by 56 chars (total length 60)
        return password != null && password.matches("^\\$2[aby]\\$.{56}$");
    }

}
