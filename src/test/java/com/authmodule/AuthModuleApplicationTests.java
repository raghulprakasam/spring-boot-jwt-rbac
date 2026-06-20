

package com.authmodule;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthModuleApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("POST /auth/register-staff - should create doctor and return Success")
    void register_success() throws Exception {
        String uniqueEmail = "testdoc_" + System.currentTimeMillis() + "@medicore.com";
        
        // Postman-la namma anupuna exact JSON data
        String jsonPayload = "{"
                + "\"firstName\": \"Test\","
                + "\"lastName\": \"Doctor\","
                + "\"email\": \"" + uniqueEmail + "\","
                + "\"password\": \"Password@123\","
                + "\"mobile\": \"9876543210\","
                + "\"dob\": \"1990-05-15\","
                + "\"role\": \"ROLE_DOCTOR\","
                + "\"department\": \"Cardiology\","
                + "\"medicalLicense\": \"MCI-123456\""
                + "}";

        // Namma puthu endpoint '/auth/register-staff' a test panrom
        mockMvc.perform(post("/auth/register-staff")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /auth/login - valid credentials should return 200 with tokens")
    void login_success() throws Exception {
        String email = "login_test_" + System.currentTimeMillis() + "@medicore.com";
        String password = "Password@123";

        // First Oru aala Register panrom
        String registerJson = "{"
                + "\"firstName\": \"Login\","
                + "\"lastName\": \"TestUser\","
                + "\"email\": \"" + email + "\","
                + "\"password\": \"" + password + "\","
                + "\"mobile\": \"9876543210\","
                + "\"dob\": \"1990-01-01\","
                + "\"role\": \"ROLE_STAFF\","
                + "\"assignedWard\": \"ICU\","
                + "\"staffRole\": \"Staff Nurse\""
                + "}";

        mockMvc.perform(post("/auth/register-staff")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson));

        // Appuram atha vachi Login panrom (Login URL usually '/auth/login' thaan irukum)
        String loginJson = "{"
                + "\"email\": \"" + email + "\","
                + "\"password\": \"" + password + "\""
                + "}";

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
    }

    @Test
    @DisplayName("POST /auth/login - wrong password should return Unauthorized")
    void login_badCredentials() throws Exception {
        String loginJson = "{"
                + "\"email\": \"nobody@medicore.com\","
                + "\"password\": \"WrongPassword!\""
                + "}";

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isUnauthorized()); // Or isBadRequest() depending on your exception handler
    }

}
