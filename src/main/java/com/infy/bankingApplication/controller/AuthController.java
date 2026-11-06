package com.infy.bankingApplication.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.infy.bankingApplication.dto.LoginRequest;
import com.infy.bankingApplication.dto.RegisterRequest;
import com.infy.bankingApplication.entity.User;
import com.infy.bankingApplication.repository.UserRepository;
import com.infy.bankingApplication.security.JwtService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
	
	private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @Valid @RequestBody RegisterRequest request) {

        if (userRepository.existsByUsername(
                request.getUsername())) {

            return ResponseEntity.badRequest()
                    .body("Username already exists");
        }

        if (userRepository.existsByEmail(
                request.getEmail())) {

            return ResponseEntity.badRequest()
                    .body("Email already exists");
        }

        User user = new User();

        user.setUsername(request.getUsername());

        user.setPassword(
                passwordEncoder.encode(
                        request.getPassword()));

        user.setEmail(request.getEmail());

        user.setRole("CUSTOMER");
        user.setStatus("ACTIVE");

        userRepository.save(user);

        return ResponseEntity.ok(
                "User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(
            @Valid @RequestBody LoginRequest request) {

        User user = userRepository
                .findByUsername(request.getUsername())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Invalid username or password"));

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new RuntimeException(
                    "Invalid username or password");
        }

        if (!"ACTIVE".equals(user.getStatus())) {

            throw new RuntimeException(
                    "User is not active");
        }

        String token = jwtService.generateToken(user);

        return ResponseEntity.ok(token);
    }

}
