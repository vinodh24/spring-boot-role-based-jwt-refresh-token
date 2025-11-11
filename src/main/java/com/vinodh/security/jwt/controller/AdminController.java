package com.vinodh.security.jwt.controller;

import com.vinodh.security.jwt.model.User;
import com.vinodh.security.jwt.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private IUserService userService;

    @GetMapping()
    public ResponseEntity<String> sayHi() {
        logger.info("Admin endpoint hit: sayHi");
        return ResponseEntity.ok("HI ADMIN");
    }

    // Create user
    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        logger.info("Admin creating user with email={}", user.getEmail());
        User saved = userService.create(user);
        saved.setPassword(null); // do not expose password
        logger.info("User created id={}, email={}", saved.getId(), saved.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // List all users
    @GetMapping("/users")
    public ResponseEntity<List<User>> listUsers() {
        logger.info("Admin listing all users");
        List<User> users = userService.getAll()
                .stream()
                .peek(u -> u.setPassword(null))
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    // Get user by id
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        logger.info("Admin get user by id={}", id);
        Optional<User> opt = userService.getById(id);
        if (opt.isEmpty()) {
            logger.warn("User not found id={}", id);
            return ResponseEntity.notFound().build();
        }
        User u = opt.get();
        u.setPassword(null);
        return ResponseEntity.ok(u);
    }

    // Update user
    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User payload) {
        logger.info("Admin updating user id={}", id);
        try {
            User saved = userService.update(id, payload);
            saved.setPassword(null);
            logger.info("User updated id={}", id);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException ex) {
            logger.warn("Update failed, user not found id={}", id);
            return ResponseEntity.notFound().build();
        }
    }

    // Delete user
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        logger.info("Admin deleting user id={}", id);
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Disable/Enable user by email
    @PatchMapping("/users/status")
    public ResponseEntity<User> changeUserStatus(@RequestParam String email, @RequestParam boolean disabled) {
        logger.info("Admin changing status for email={}, disabled={}", email, disabled);
        try {
            User user = userService.getByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            user.setIsDisabled(disabled);
            User saved = userService.update(user.getId().longValue(), user);
            logger.info("User status changed email={}, disabled={}", email, disabled);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException ex) {
            logger.warn("Change status failed for email={}", email);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/users/disabled")
    public ResponseEntity<List<User>> listDisabledUsers() {
        logger.info("Admin listing disabled users");
        List<User> disabledUsers = userService.getAll()
                .stream()
                .filter(User::getIsDisabled)
                .peek(u -> u.setPassword(null))
                .collect(Collectors.toList());
        return ResponseEntity.ok(disabledUsers);
    }

}
