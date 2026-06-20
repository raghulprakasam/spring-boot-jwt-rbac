package com.authmodule.controller;

import com.authmodule.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

  
    @GetMapping("/user")
    @PreAuthorize("hasAnyRole('USER', 'MODERATOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<String>> userAccess() {
        return ResponseEntity.ok(
                ApiResponse.success("User content", "Hello! You have at least ROLE_USER."));
    }

  
    @GetMapping("/moderator")
    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<String>> moderatorAccess() {
        return ResponseEntity.ok(
                ApiResponse.success("Moderator content", "Welcome, moderator!"));
    }

    
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> adminAccess() {
        return ResponseEntity.ok(
                ApiResponse.success("Admin content", "Welcome, admin! You have full access."));
    }

  
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        Map<String, Object> userInfo = Map.of(
                "email",       auth.getName(),
                "roles",       auth.getAuthorities()
                                    .stream()
                                    .map(a -> a.getAuthority())
                                    .toList(),
                "authenticated", auth.isAuthenticated()
        );

        return ResponseEntity.ok(ApiResponse.success("Current user info", userInfo));
    }
}
