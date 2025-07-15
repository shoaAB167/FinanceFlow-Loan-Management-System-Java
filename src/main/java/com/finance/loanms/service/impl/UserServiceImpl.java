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
        if (userRepo.findByUsername(username).isPresent()) {
            return ApiResponse.fail("Username already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(Set.of(Role.USER));
        userRepo.save(user);

        return ApiResponse.ok("User registered successfully", null);
    }

    @Override
    public ApiResponse<Map<String, String>> login(String username, String password) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        Map<String, String> tokens = Map.of(
                "accessToken", jwtTokenProvider.generateAccessToken(user.getUsername(), user.getRoles()),
                "refreshToken", jwtTokenProvider.generateRefreshToken(user.getUsername())
        );

        return ApiResponse.ok("Login successful", tokens);
    }

    @Override
    public ApiResponse<Map<String, String>> refresh(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid or expired refresh token");
        }

        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Map<String, String> tokens = Map.of(
                "accessToken", jwtTokenProvider.generateAccessToken(user.getUsername(), user.getRoles())
        );

        return ApiResponse.ok("Token refreshed successfully", tokens);
    }
}

