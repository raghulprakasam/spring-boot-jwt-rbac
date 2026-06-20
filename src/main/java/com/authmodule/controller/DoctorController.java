package com.authmodule.controller;

import com.authmodule.entity.Appointment;
import com.authmodule.entity.User;
import com.authmodule.repository.AppointmentRepository;
import com.authmodule.repository.PrescriptionRepository;
import com.authmodule.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/doctor")
@RequiredArgsConstructor
public class DoctorController {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final PrescriptionRepository prescriptionRepository;

   
    @GetMapping("/all-patients")
    public ResponseEntity<List<Appointment>> getAllAppointments() {
        return ResponseEntity.ok(appointmentRepository.findAll());
    }

   
    @GetMapping("/search-patients")
    public ResponseEntity<List<Appointment>> searchPatients(
            @RequestParam String name, 
            Authentication authentication) {
        
        String email = authentication.getName();
        String dept = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Doctor not found"))
                .getDepartment();
        
        return ResponseEntity.ok(appointmentRepository.findByDepartmentAndPatientFirstNameContainingIgnoreCase(dept, name));
    }


    @PutMapping("/update/{appointmentId}")
    public ResponseEntity<?> updateDiagnosis(@PathVariable Long appointmentId, @RequestBody Appointment updateDetails) {
        try {
            System.out.println("Updating ID: " + appointmentId);
            
       
            Appointment appointment = appointmentRepository.findById(appointmentId)
                    .orElseThrow(() -> new RuntimeException("Appointment not found"));
            
         
            if (updateDetails == null) {
                return ResponseEntity.badRequest().body("Request body is empty");
            }

        
            if (updateDetails.getDiagnosis() != null) appointment.setDiagnosis(updateDetails.getDiagnosis());
            if (updateDetails.getPrescribedMedicine() != null) appointment.setPrescribedMedicine(updateDetails.getPrescribedMedicine());
            
            appointment.setStatus("COMPLETED");
            appointmentRepository.save(appointment);

            return ResponseEntity.ok("Diagnosis updated successfully!");
            
        } catch (Exception e) {
            e.printStackTrace(); 
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

   
    @GetMapping("/profile")
    public ResponseEntity<?> getDoctorProfile(Authentication authentication) {
        String email = authentication.getName();
        User doctor = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        
        Map<String, String> response = new HashMap<>();
        response.put("name", "Dr. " + doctor.getFirstName());
        response.put("dept", doctor.getDepartment());
        return ResponseEntity.ok(response);
    }
}