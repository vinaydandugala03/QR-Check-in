package com.eventticketing.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.eventticketing.dto.PasswordChangeRequest;
import com.eventticketing.entity.User;
import com.eventticketing.exception.EmailAlreadyExistsException;
import com.eventticketing.repository.BookingRepository;
import com.eventticketing.repository.UserRepository;
import com.eventticketing.security.UserPrincipal;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            BookingRepository bookingRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(User user) {
        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());

        if (existingUser.isPresent()) {
            throw new EmailAlreadyExistsException("Email already registered");
        }

        // Strict role control: public registration ALWAYS assigns USER
        user.setRole("USER");

        // Hash password before saving to database
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found with ID: " + id));
    }

    public User getUserById(Long id, UserPrincipal principal) {
        verifyUserOwnership(id, principal, "view this profile");
        return getUserById(id);
    }

    public User updateUser(Long id, User updatedUser, UserPrincipal principal) {
        verifyUserOwnership(id, principal, "update this profile");

        User existingUser = getUserById(id);

        Optional<User> userWithEmail = userRepository.findByEmail(updatedUser.getEmail());

        if (userWithEmail.isPresent() && !userWithEmail.get().getId().equals(id)) {
            throw new EmailAlreadyExistsException("Email already registered");
        }

        existingUser.setName(updatedUser.getName());
        existingUser.setEmail(updatedUser.getEmail());

        // Note: Password and Role are intentionally NOT overwritten here
        return userRepository.save(existingUser);
    }

    public void changePassword(Long id, PasswordChangeRequest request, UserPrincipal principal) {
        verifyUserOwnership(id, principal, "change password for this user");

        User user = getUserById(id);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadCredentialsException("Current password does not match");
        }

        if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
            throw new IllegalArgumentException("New password must contain at least 6 characters");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public void deleteUser(Long id, UserPrincipal principal) {
        verifyUserOwnership(id, principal, "delete this account");

        User user = getUserById(id);

        if (bookingRepository.existsByUserId(id)) {
            throw new IllegalStateException("Cannot delete a user with booking history");
        }

        userRepository.delete(user);
    }

    private void verifyUserOwnership(Long targetUserId, UserPrincipal principal, String action) {
        if (principal == null) {
            throw new AccessDeniedException("Authentication required to " + action);
        }

        boolean isAdmin = "ADMIN".equalsIgnoreCase(principal.getRole());
        boolean isOwner = principal.getId() != null && principal.getId().equals(targetUserId);

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("Access denied: You are not authorized to " + action);
        }
    }
}