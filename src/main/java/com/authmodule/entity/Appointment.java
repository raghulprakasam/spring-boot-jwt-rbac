package com.authmodule.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "appointments")
@Data
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

 
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "patient_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Patient patient;

    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "doctor_id", nullable = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User doctor;
    private String department;
    private LocalDate date;
    private String paymentMode;
    private String status;

  
    private String diagnosis;
    private String prescribedMedicine;
    private String labTests;

    
    
    @JsonProperty("appointmentId")
    public Long getAppointmentId() {
        return this.id;
    }

    public Long getPatientId() {
        return this.patient != null ? this.patient.getId() : null;
    }
    
 
    public String getDoctorEmail() {
        return this.doctor != null ? this.doctor.getEmail() : null;
    }
}