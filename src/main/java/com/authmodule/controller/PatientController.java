package com.authmodule.controller;

import com.authmodule.entity.User;
import com.authmodule.entity.Patient;
import com.authmodule.entity.Appointment;
import com.authmodule.repository.UserRepository;
import com.authmodule.repository.AppointmentRepository;
import com.authmodule.repository.PatientRepository;
import com.authmodule.entity.AppointmentRequest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.authmodule.entity.Role; // உன்னுடைய Role கிளாஸ் இருக்கும் Package பெயரில் மாற்றிக்கொள்

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/patient")
@RequiredArgsConstructor
public class PatientController {

    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> response = new HashMap<>();
        response.put("firstName", user.getFirstName());
        response.put("lastName", user.getLastName());
        response.put("email", user.getEmail());
        response.put("mobile", user.getMobile());
        response.put("dob", user.getDob());
        response.put("patientId", "PAT" + String.format("%05d", user.getId()));

        return ResponseEntity.ok(response);
    }

    
    @PostMapping("/appointments/book")
    public ResponseEntity<?> bookAppointment(@RequestBody Appointment appointment, Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Patient patient = patientRepository.findByUser(user);
        if (patient == null) {
            patient = new Patient();
            patient.setUser(user);
            patient = patientRepository.save(patient);
        }
        
        appointment.setPatient(patient);
        
        
        if (appointment.getDepartment() != null) {
            List<User> doctors = userRepository.findByDepartmentAndRole(appointment.getDepartment(), Role.ROLE_DOCTOR);
            if (!doctors.isEmpty()) {
                appointment.setDoctor(doctors.get(0)); // முதல் டாக்டரை அசைன் செய்கிறோம்
            }
        }
        
        return ResponseEntity.ok(appointmentRepository.save(appointment));
    }
    @GetMapping("/appointments")
    public ResponseEntity<List<Appointment>> getMyAppointments(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Patient patient = patientRepository.findByUser(user);
        
 
        if (patient == null) {
            return ResponseEntity.ok(java.util.Collections.emptyList());
        }
        
        return ResponseEntity.ok(appointmentRepository.findByPatient(patient));
    }
}