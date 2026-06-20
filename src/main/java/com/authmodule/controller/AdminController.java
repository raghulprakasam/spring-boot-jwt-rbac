

package com.authmodule.controller;

import com.authmodule.dto.request.RegisterRequest;
import com.authmodule.dto.response.ApiResponse;
import com.authmodule.dto.response.AuthResponse;
import com.authmodule.entity.Role;
import com.authmodule.entity.User;
import com.authmodule.repository.UserRepository;
import com.authmodule.repository.PatientRepository;     // இம்போர்ட் சேர்க்கவும்
import com.authmodule.repository.AppointmentRepository; // இம்போர்ட் சேர்க்கவும்
import com.authmodule.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final AuthService authService;

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @PostMapping("/create-staff")
    public ResponseEntity<ApiResponse<AuthResponse>> createStaff(
            @RequestBody RegisterRequest request, 
            @RequestParam Role role) {
        
        AuthResponse response = authService.registerStaff(request, role);
        return ResponseEntity.ok(ApiResponse.success("Staff created successfully", response));
    }

    @GetMapping("/profile")
    public ResponseEntity<User> getAdminProfile(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(user);
    }


    @GetMapping("/patients/count")
    public ResponseEntity<Long> getPatientCount() {
        return ResponseEntity.ok(patientRepository.count());
    }

 
    @GetMapping("/appointments/today")
    public ResponseEntity<Long> getTodayAppointmentCount() {
        return ResponseEntity.ok(appointmentRepository.countTodayAppointments());
    }
    

    @GetMapping("/patients/search")
    public ResponseEntity<ApiResponse<?>> searchPatient(@RequestParam String query) {
        return patientRepository.findByPatientId(query)
                .<ResponseEntity<ApiResponse<?>>>map(patient -> 
                    ResponseEntity.ok(ApiResponse.success("Patient found", patient))
                )
                .orElseGet(() -> 
                    ResponseEntity.ok(ApiResponse.error("Patient not found"))
                );
    }
}