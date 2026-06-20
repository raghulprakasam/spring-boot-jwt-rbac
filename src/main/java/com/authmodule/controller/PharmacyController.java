package com.authmodule.controller;

import com.authmodule.entity.Prescription;
import com.authmodule.repository.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/pharmacy")
@RequiredArgsConstructor
public class PharmacyController {

    private final PrescriptionRepository prescriptionRepository;


    @GetMapping("/pending")
    public ResponseEntity<List<Prescription>> getPendingPrescriptions() {
     
        return ResponseEntity.ok(prescriptionRepository.findByStatus("PENDING"));
    }

 
    @PutMapping("/issue/{id}")
    public ResponseEntity<?> issueMedicine(@PathVariable Long id) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prescription not found"));

        prescription.setStatus("ISSUED");
        prescription.setIssuedAt(LocalDateTime.now());
        
        prescriptionRepository.save(prescription);

        return ResponseEntity.ok("Medicine issued successfully!");
    }
}