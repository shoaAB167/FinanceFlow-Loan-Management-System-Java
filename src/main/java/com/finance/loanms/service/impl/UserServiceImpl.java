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

import com.finance.loanms.dto.request.LoginRequest;
import com.finance.loanms.dto.request.RegisterRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Map;
import java.util.Set;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public UserServiceImpl(UserRepository userRepo, PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider, AuthenticationManager authenticationManager) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public ApiResponse<String> register(RegisterRequest request) {
        try {
            log.info("Attempting to register user: {}", request.getUsername());

            // Check for existing username
            if (userRepo.findByUsername(request.getUsername()).isPresent()) {
                throw new IllegalArgumentException("Username already exists");
            }

            // Create and save new user
            User user = new User();
            user.setUsername(request.getUsername().trim());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setRoles(Set.of(Role.USER));
            userRepo.save(user);

            log.info("User registered successfully: {}", request.getUsername());
            return ApiResponse.ok("User registered successfully", null);

        } catch (IllegalArgumentException e) {
            log.warn("Registration failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Registration error", e);
            throw new RuntimeException("Failed to register user: " + e.getMessage(), e);
        }
    }

    @Override
    public ApiResponse<Map<String, String>> login(LoginRequest request) {
        try {
            log.info("Attempting login for user: {}", request.getUsername());

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

            User user = userRepo.findByUsername(request.getUsername())
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            // Generate tokens
            Map<String, String> tokens = Map.of(
                    "accessToken", jwtTokenProvider.generateAccessToken(user.getUsername(), user.getRoles()),
                    "refreshToken", jwtTokenProvider.generateRefreshToken(user.getUsername()));

            log.info("Login successful for user: {}", request.getUsername());
            return ApiResponse.ok("Login successful", tokens);

        } catch (Exception e) {
            log.error("Login failed for user: {}", request.getUsername(), e);
            throw new RuntimeException("Invalid username or password");
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
                    "accessToken", jwtTokenProvider.generateAccessToken(user.getUsername(), user.getRoles()));

            return ApiResponse.ok("Token refreshed successfully", tokens);
        } catch (IllegalArgumentException | UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to refresh token: " + e.getMessage(), e);
        }
    }
}
