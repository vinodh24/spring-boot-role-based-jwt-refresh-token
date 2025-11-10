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

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements IUserService {

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetailsService userDetailsService() {
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                return userRepository.findByEmail(username)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            }
        };
    }

    // CRUD operations for admin use

    @Override
    public User create(User user) {
        user.setId(null);
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }

    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> getById(Long id) {
        return userRepository.findById(id.intValue());
    }

    @Override
    public User update(Long id, User payload) {
        User existing = userRepository.findById(id.intValue()).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (payload.getFirstName() != null) existing.setFirstName(payload.getFirstName());
        if (payload.getLastName() != null) existing.setLastName(payload.getLastName());
        if (payload.getEmail() != null) existing.setEmail(payload.getEmail());
        if (payload.getRole() != null) existing.setRole(payload.getRole());
        /*if (payload.getPassword() != null && !payload.getPassword().isEmpty()) {
            String newPassword = payload.getPassword();

            if (!isPasswordEncoded(newPassword)) {
                // Only encode if it's plain text
                existing.setPassword(passwordEncoder.encode(newPassword));
            } else {
                // Already encoded (came from DB or admin UI) → just reuse
                existing.setPassword(newPassword);
            }
            existing.setPassword(passwordEncoder.encode(payload.getPassword()));
        }*/
        return userRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        userRepository.deleteById(id.intValue());
    }

    @Override
    public Optional<User> getByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    private boolean isPasswordEncoded(String password) {
        // bcrypt hashes look like: $2a$, $2b$, or $2y$ followed by 56 chars (total length 60)
        return password != null && password.matches("^\\$2[aby]\\$.{56}$");
    }

}
