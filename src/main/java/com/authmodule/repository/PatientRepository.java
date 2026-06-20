package com.authmodule.repository;

import com.authmodule.entity.Patient;
import com.authmodule.entity.User; // இதையும் இம்போர்ட் செய்ய மறக்காதீர்கள்
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    
   
    Patient findByUser(User user); 
    
 
    Optional<Patient> findByPatientId(String patientId);
}