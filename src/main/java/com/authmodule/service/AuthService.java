package com.authmodule.service;

import com.authmodule.dto.request.LoginRequest;
import com.authmodule.dto.request.RefreshTokenRequest;
import com.authmodule.dto.request.RegisterRequest;
import com.authmodule.dto.response.AuthResponse;
import com.authmodule.entity.RefreshToken;
import com.authmodule.entity.Role;
import com.authmodule.entity.User;
import com.authmodule.exception.EmailAlreadyExistsException;
import com.authmodule.repository.UserRepository;
import com.authmodule.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;


    
    @Transactional
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

    
        Role role = Role.ROLE_USER; 
        
        if (request.getRole() != null && !request.getRole().trim().isEmpty()) {
            try {
          
                role = Role.valueOf(request.getRole().toUpperCase());
            } catch (IllegalArgumentException e) {
                log.error("Invalid role provided: {}", request.getRole());
                throw new RuntimeException("Invalid role specified! Please provide a valid role like ROLE_DOCTOR");
            }
        }

      
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dob(request.getDob())
                .mobile(request.getMobile())
                .email(request.getEmail().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(EnumSet.of(role)) // 
                .department(request.getDepartment())
                .medicalLicense(request.getMedicalLicense())
                .build();

        User savedUser = userRepository.save(user);

        log.info("New user registered: {} with Role: {}", savedUser.getEmail(), role.name());

        String accessToken = jwtService.generateToken(savedUser);
        RefreshToken refreshToken =
                refreshTokenService.createRefreshToken(savedUser);

        return buildAuthResponse(
                savedUser,
                accessToken,
                refreshToken.getToken()
        );
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().toLowerCase(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(
                        request.getEmail().toLowerCase())
                .orElseThrow(() -> new RuntimeException("User not found"));

     
        if (user.getRoles().contains(Role.ROLE_DOCTOR)) {
        
            if (user.getDepartment() == null || user.getDepartment().trim().isEmpty()) {
                throw new RuntimeException("Doctor profile incomplete: Department not assigned!");
            }
        }
        // ------------------------------------

        String accessToken = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        log.info("User logged in: {}", user.getEmail());

        return buildAuthResponse(
                user,
                accessToken,
                refreshToken.getToken()
        );
    }
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {

        RefreshToken verified =
                refreshTokenService.verifyExpiration(
                        request.getRefreshToken()
                );

        User user = verified.getUser();

        String newAccessToken =
                jwtService.generateToken(user);

        return buildAuthResponse(
                user,
                newAccessToken,
                verified.getToken()
        );
    }

    private AuthResponse buildAuthResponse(
            User user,
            String accessToken,
            String refreshToken) {

        List<String> roles = user.getRoles()
                .stream()
                .map(Role::name)
                .collect(Collectors.toList());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getJwtExpiration())
                .email(user.getEmail())
                .roles(roles)
                .build();
    }


    @Transactional
    public AuthResponse registerStaff(RegisterRequest request, Role role) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

     
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setDob(request.getDob());
        user.setMobile(request.getMobile());
        user.setEmail(request.getEmail().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(EnumSet.of(role));
        
      
        user.setDepartment(request.getDepartment());
        user.setMedicalLicense(request.getMedicalLicense());
        user.setAssignedWard(request.getAssignedWard());
        user.setStaffRole(request.getStaffRole());
        user.setFinancialRole(request.getFinancialRole());
        user.setEmployeeId(request.getEmployeeId());

      
        User savedUser = userRepository.save(user);
        
        refreshTokenService.createRefreshToken(savedUser);
        
        return buildAuthResponse(savedUser, jwtService.generateToken(savedUser), "N/A");
    }
}