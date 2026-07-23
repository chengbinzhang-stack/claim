package com.insurance.claimapi.controller;

import com.insurance.claimapi.entity.*;
import com.insurance.claimapi.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class AuthControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private Role customerRole;

    @BeforeEach
    void setUp() {
        customerRole = roleRepository.findByName("CUSTOMER").orElseGet(() ->
                roleRepository.save(Role.builder().name("CUSTOMER").build()));
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /auth/login - valid credentials returns token")
    void login_success() throws Exception {
        userRepository.save(User.builder()
                .username("logintest").password(passwordEncoder.encode("password123"))
                .email("login@test.com").fullName("Login Test")
                .role(customerRole).enabled(true).build());

        String body = "{\"username\": \"logintest\", \"password\": \"password123\"}";

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token", notNullValue()))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.user.username").value("logintest"));
    }

    @Test
    @DisplayName("POST /auth/login - wrong password returns 401")
    void login_wrongPassword() throws Exception {
        userRepository.save(User.builder()
                .username("logintest2").password(passwordEncoder.encode("correctpassword"))
                .email("login2@test.com").fullName("Login Test 2")
                .role(customerRole).enabled(true).build());

        String body = "{\"username\": \"logintest2\", \"password\": \"wrongpassword\"}";

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /auth/login - non-existent user returns 401")
    void login_userNotFound() throws Exception {
        String body = "{\"username\": \"nonexistent\", \"password\": \"anypassword\"}";

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /auth/register - valid request creates user and returns token")
    void register_success() throws Exception {
        String body = "{\"username\": \"newuser\", \"password\": \"password123\", \"email\": \"newuser@test.com\", \"fullName\": \"New User\"}";

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token", notNullValue()))
                .andExpect(jsonPath("$.data.user.username").value("newuser"))
                .andExpect(jsonPath("$.data.user.email").value("newuser@test.com"));
    }

    @Test
    @DisplayName("POST /auth/register - duplicate username returns 400")
    void register_duplicateUsername() throws Exception {
        userRepository.save(User.builder()
                .username("existinguser").password(passwordEncoder.encode("pass"))
                .email("existing@test.com").fullName("Existing")
                .role(customerRole).enabled(true).build());

        String body = "{\"username\": \"existinguser\", \"password\": \"password123\", \"email\": \"newemail@test.com\", \"fullName\": \"New Name\"}";

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /auth/register - missing required fields returns 400")
    void register_validationError() throws Exception {
        String body = "{\"username\": \"\", \"password\": \"\", \"email\": \"notanemail\"}";

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
