package com.authmodule.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "patients")
@Data
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

   
    @OneToOne
    @JoinColumn(name = "user_id") 
    private User user;

    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDate dob;
    private String patientId;
}