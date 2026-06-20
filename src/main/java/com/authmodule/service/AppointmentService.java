package com.authmodule.service;

import com.authmodule.entity.Appointment;
import com.authmodule.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;


    public List<Appointment> getAppointmentsByDoctorEmail(String email) {
        return appointmentRepository.findByDoctorEmail(email);
    }

  
    @Transactional
    public Appointment bookAppointment(Appointment appointment) {

        if (appointment == null) {
            throw new IllegalArgumentException("Appointment details cannot be null");
        }

  
        if (appointment.getStatus() == null) {
            appointment.setStatus("PENDING");
        }

     
        return appointmentRepository.save(appointment);
    }

  
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }
}