package com.authmodule.repository;

import com.authmodule.entity.Appointment;
import com.authmodule.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    
  
	@Query("SELECT a FROM Appointment a WHERE a.doctor.email = :email")
	List<Appointment> findByDoctorEmail(@Param("email") String email);
    
    
    boolean existsByPatientAndDate(Patient patient, LocalDate date);
    
    List<Appointment> findByPatient(Patient patient);
    
    List<Appointment> findAll();
    
    List<Appointment> findByDepartmentAndDate(String department, LocalDate date);
    
    List<Appointment> findByDepartmentAndPatientFirstNameContainingIgnoreCase(String department, String name);
    
    @Query("SELECT count(a) FROM Appointment a WHERE a.date = CURRENT_DATE")
    long countTodayAppointments();
    
    List<Appointment> findByDepartment(String department);
}