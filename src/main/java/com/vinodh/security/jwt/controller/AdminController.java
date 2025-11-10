package com.vinodh.security.jwt.controller;

import com.vinodh.security.jwt.model.User;
import com.vinodh.security.jwt.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    @Autowired
    private IUserService userService;

    @GetMapping()
    public ResponseEntity<String> sayHi() {
        return ResponseEntity.ok("HI ADMIN");
    }

    // Create user
    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User saved = userService.create(user);
        saved.setPassword(null); // do not expose password
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // List all users
    @GetMapping("/users")
    public ResponseEntity<List<User>> listUsers() {
        List<User> users = userService.getAll()
                .stream()
                .peek(u -> u.setPassword(null))
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    // Get user by id
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        Optional<User> opt = userService.getById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        User u = opt.get();
        u.setPassword(null);
        return ResponseEntity.ok(u);
    }

    // Update user
    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User payload) {
        try {
            User saved = userService.update(id, payload);
            saved.setPassword(null);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete user
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Disable/Enable user by email
    @PatchMapping("/users/status")
    public ResponseEntity<User> changeUserStatus(@RequestParam String email, @RequestParam boolean disabled) {
        try {
            User user = userService.getByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            user.setIsDisabled(disabled);
            User saved = userService.update(user.getId().longValue(), user);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/users/disabled")
    public ResponseEntity<List<User>> listDisabledUsers() {
        List<User> disabledUsers = userService.getAll()
                .stream()
                .filter(User::getIsDisabled)
                .peek(u -> u.setPassword(null))
                .collect(Collectors.toList());
        return ResponseEntity.ok(disabledUsers);
    }

}
