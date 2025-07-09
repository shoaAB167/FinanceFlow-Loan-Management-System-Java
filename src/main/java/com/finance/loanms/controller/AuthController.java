package com.finance.loanms.controller;

import com.finance.loanms.model.entity.Role;
import com.finance.loanms.model.entity.User;
import com.finance.loanms.repository.UserRepository;
import com.finance.loanms.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(JwtTokenProvider jwtTokenProvider, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public String register(@RequestBody Map<String, String> req) {
        User user = new User();
        user.setUsername(req.get("username"));
        user.setPassword(passwordEncoder.encode(req.get("password")));
        user.setRoles(Set.of(Role.USER));
        userRepository.save(user);
        return "User registered!";
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> req) {
        var user = userRepository.findByUsername(req.get("username"))
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(req.get("password"), user.getPassword()))
            throw new RuntimeException("Invalid password");

        return Map.of(
                "accessToken", jwtTokenProvider.generateAccessToken(user.getUsername(), user.getRoles()),
                "refreshToken", jwtTokenProvider.generateRefreshToken(user.getUsername())
        );
    }
}

