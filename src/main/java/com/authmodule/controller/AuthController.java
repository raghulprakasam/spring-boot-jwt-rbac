package com.authmodule.controller;

import com.authmodule.dto.request.LoginRequest;
import com.authmodule.dto.request.RefreshTokenRequest;
import com.authmodule.dto.request.RegisterRequest;
import com.authmodule.dto.response.ApiResponse;
import com.authmodule.dto.response.AuthResponse;
import com.authmodule.service.AuthService;
import com.authmodule.repository.UserRepository;
import com.authmodule.entity.User;
import com.authmodule.entity.Role;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Success", authService.register(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Login successful", authService.login(request)));
    }

    @PostMapping("/register-staff")
    public ResponseEntity<ApiResponse<AuthResponse>> registerStaff(@Valid @RequestBody RegisterRequest request) {
        Role role = Role.valueOf(request.getRole());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Staff registered", authService.registerStaff(request, role)));
    }

    @GetMapping("/profile")
    public ResponseEntity<User> getProfile(Authentication authentication) {
        return ResponseEntity.ok(userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found")));
    }
}