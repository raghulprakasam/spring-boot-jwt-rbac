package com.authmodule.entity; // நீ எங்கு வைக்கிறாயோ அந்த பேக்கேஜ் பெயரைப் போடு

import lombok.Data;
import java.time.LocalDate;

@Data
public class AppointmentRequest {
    private Long doctorId;
    private LocalDate date;
    private String department;
}