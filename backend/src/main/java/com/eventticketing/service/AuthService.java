package com.eventticketing.service;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.eventticketing.dto.LoginRequest;
import com.eventticketing.dto.LoginResponse;
import com.eventticketing.dto.UserResponse;
import com.eventticketing.entity.User;
import com.eventticketing.repository.UserRepository;
import com.eventticketing.security.JwtService;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String role = user.getRole() != null ? user.getRole() : "USER";
        String token = jwtService.generateToken(user.getEmail(), role, user.getId());

        UserResponse userResponse = new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                role
        );

        return new LoginResponse(token, userResponse);
    }
}
