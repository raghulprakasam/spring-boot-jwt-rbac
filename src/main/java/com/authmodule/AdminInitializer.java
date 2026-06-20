package com.authmodule;

import com.authmodule.entity.Role;
import com.authmodule.entity.User;
import com.authmodule.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDate; // இதைச் சேர்க்க மறக்காதீர்கள்
import java.util.EnumSet;

@Configuration
public class AdminInitializer {

    @Bean
    public CommandLineRunner initAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
        	if (!userRepository.existsByEmail("admin@medcare.com")) {
        	    User admin = new User();
        	    admin.setFirstName("Admin");
        	    admin.setLastName("Medicore");
        	    admin.setEmail("admin@medcare.com");
        	    admin.setPassword(passwordEncoder.encode("ragul"));
        	    admin.setDob(LocalDate.of(1990, 1, 1));
        	    admin.setMobile("0000000000");
        	    
        	
        	    
        	    admin.setRoles(EnumSet.of(Role.ROLE_ADMIN));
        	
        	    
        	    userRepository.save(admin);
        	    System.out.println("--- Admin account created successfully! ---");
        	}
        };
    }
}