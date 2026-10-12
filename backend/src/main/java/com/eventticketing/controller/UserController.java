package com.eventticketing.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.eventticketing.dto.PasswordChangeRequest;
import com.eventticketing.dto.UserResponse;
import com.eventticketing.entity.User;
import com.eventticketing.security.UserPrincipal;
import com.eventticketing.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse registerUser(@Valid @RequestBody User user) {
        User saved = userService.registerUser(user);
        return toResponse(saved);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers().stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public UserResponse getUserById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        User user = userService.getUserById(id, principal);
        return toResponse(user);
    }

    @PutMapping("/{id}")
    public UserResponse updateUser(
            @PathVariable Long id,
            @Valid @RequestBody User user,
            @AuthenticationPrincipal UserPrincipal principal) {
        User updated = userService.updateUser(id, user, principal);
        return toResponse(updated);
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<String> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody PasswordChangeRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        userService.changePassword(id, request, principal);
        return ResponseEntity.ok("Password changed successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        userService.deleteUser(id, principal);
        return ResponseEntity.ok("User deleted successfully");
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }
}