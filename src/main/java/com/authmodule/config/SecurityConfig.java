package com.authmodule.config;

import com.authmodule.security.jwt.JwtAuthenticationFilter;
import com.authmodule.security.services.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Central Spring Security configuration.
 *
 * Key decisions:
 *  - CSRF disabled  → REST APIs are stateless; clients authenticate via JWT, not cookies.
 *  - Stateless session → no HttpSession is created; every request must carry its token.
 *  - @EnableMethodSecurity → activates @PreAuthorize / @PostAuthorize on service/controller methods.
 *  - DaoAuthenticationProvider → wires BCrypt + our custom UserDetailsService together.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity           // enables @PreAuthorize, @PostAuthorize, @Secured
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter  jwtAuthFilter;
    private final UserDetailsServiceImpl   userDetailsService;

    /** Public endpoints that bypass JWT verification entirely. */
    private static final String[] PUBLIC_ENDPOINTS = {
            "/auth/register",
            "/auth/login",
            "/auth/refresh",
            "/actuator/health",

            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };
    // ── Security Filter Chain ────────────────────────────────────────────────

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http

        
        .cors(cors -> cors.configurationSource(request -> {
            org.springframework.web.cors.CorsConfiguration config = new org.springframework.web.cors.CorsConfiguration();
            
    
            config.setAllowedOrigins(java.util.List.of(
                    "http://127.0.0.1:5500",
                    "http://localhost:5500",
                    "http://localhost:3000"
            ));
            
            config.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
            config.setAllowedHeaders(java.util.List.of("*"));
            
        
            config.setAllowCredentials(true); 
            config.setMaxAge(3600L); // பிரவுசர் CORS செக்கிற்கு 1 மணிநேரம் அனுமதி அளிக்கும்
            
            return config;
        }))

            .csrf(csrf -> csrf.disable())

      
            
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(
                            "/auth/**",
                            "/actuator/health",
                            "/v3/api-docs/**",
                            "/swagger-ui/**"
                    ).permitAll()
                    .requestMatchers("/admin/**").hasRole("ADMIN")
           
                    .requestMatchers("/doctor/**").authenticated()
                    .requestMatchers("/pharmacy/**").authenticated()
                    .anyRequest().authenticated()
            )
            .sessionManagement(sess ->
                    sess.sessionCreationPolicy(
                            org.springframework.security.config.http.SessionCreationPolicy.STATELESS
                    )
            )

            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter,
                    org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
    // ── Authentication Beans ─────────────────────────────────────────────────

    /**
     * DaoAuthenticationProvider connects UserDetailsService + PasswordEncoder
     * so AuthenticationManager can authenticate username/password credentials.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * AuthenticationManager is used by the AuthService to authenticate
     * login requests programmatically.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * BCrypt with strength 12. Strength 10 is the default; 12 adds extra
     * security at the cost of slightly higher CPU per login (acceptable).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
    
}
