package com.finance.loanms.service.impl;

import com.finance.loanms.dto.ApiResponse;
import com.finance.loanms.exception.UserNotFoundException;
import com.finance.loanms.model.enumtype.Role;
import com.finance.loanms.model.entity.User;
import com.finance.loanms.repository.UserRepository;
import com.finance.loanms.security.JwtTokenProvider;
import com.finance.loanms.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public UserServiceImpl(UserRepository userRepo, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public ApiResponse<String> register(String username, String password) {
        try {
            if (username == null || username.trim().isEmpty()) {
                throw new IllegalArgumentException("Username is required");
            }
            
            if (password == null || password.trim().isEmpty()) {
                throw new IllegalArgumentException("Password is required");
            }
            
            // Validate username format (alphanumeric with underscores, 3-20 characters)
            if (!username.matches("^[a-zA-Z0-9_]{3,20}$")) {
                throw new IllegalArgumentException("Username must be 3-20 characters long and can only contain letters, numbers, and underscores");
            }
            
            // Validate password strength (at least 8 characters, with at least one letter and one number)
            if (!password.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$")) {
                throw new IllegalArgumentException("Password must be at least 8 characters long and contain at least one letter and one number");
            }
            
            // Check for existing username
            if (userRepo.findByUsername(username).isPresent()) {
                throw new IllegalArgumentException("Username already exists");
            }

            // Create and save new user
            User user = new User();
            user.setUsername(username.trim());
            user.setPassword(passwordEncoder.encode(password));
            user.setRoles(Set.of(Role.USER));
            userRepo.save(user);

            return ApiResponse.ok("User registered successfully", null);
            
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to register user: " + e.getMessage(), e);
        }
    }

    @Override
    public ApiResponse<Map<String, String>> login(String username, String password) {
        try {
            // Validate input parameters
            if (username == null || username.trim().isEmpty()) {
                throw new IllegalArgumentException("Username is required");
            }
            
            if (password == null || password.trim().isEmpty()) {
                throw new IllegalArgumentException("Password is required");
            }
            
            // Trim username for consistency
            username = username.trim();
            
            // Check if user exists
            User user = userRepo.findByUsername(username)
                    .orElseThrow(() -> new UserNotFoundException("Invalid username or password"));

            // Verify password
            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new IllegalArgumentException("Invalid username or password");
            }

            // Generate tokens
            Map<String, String> tokens = Map.of(
                    "accessToken", jwtTokenProvider.generateAccessToken(user.getUsername(), user.getRoles()),
                    "refreshToken", jwtTokenProvider.generateRefreshToken(user.getUsername())
            );

            return ApiResponse.ok("Login successful", tokens);
            
        } catch (UserNotFoundException | IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to login: " + e.getMessage(), e);
        }
    }

    @Override
    public ApiResponse<Map<String, String>> refresh(String refreshToken) {
        try {
            if (!jwtTokenProvider.validateToken(refreshToken)) {
                throw new IllegalArgumentException("Invalid or expired refresh token");
            }

            String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
            User user = userRepo.findByUsername(username)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            Map<String, String> tokens = Map.of(
                    "accessToken", jwtTokenProvider.generateAccessToken(user.getUsername(), user.getRoles())
            );

            return ApiResponse.ok("Token refreshed successfully", tokens);
        } catch (IllegalArgumentException | UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to refresh token: " + e.getMessage(), e);
        }
    }
}

