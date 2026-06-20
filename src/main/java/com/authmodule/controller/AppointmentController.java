package com.authmodule.controller;

import com.authmodule.entity.Appointment;
import com.authmodule.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;


    @GetMapping("/my-appointments")
    public ResponseEntity<List<Appointment>> getMyPatients(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(appointmentService.getAppointmentsByDoctorEmail(email));
    }

    @PostMapping("/book")
    public ResponseEntity<Appointment> bookAppointment(@RequestBody Appointment appointment) {

        Appointment savedAppointment = appointmentService.bookAppointment(appointment);
        return ResponseEntity.ok(savedAppointment);
    }
}