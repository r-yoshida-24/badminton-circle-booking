package com.badminton.controller;

import com.badminton.config.SecurityConfig;
import com.badminton.dto.RegistrationRequest;
import com.badminton.entity.User;
import com.badminton.repository.UserRepository;
import com.badminton.service.UserRegistrationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.mockito.ArgumentCaptor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RegistrationController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class RegistrationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRegistrationService userRegistrationService;

    @MockBean
    private UserRepository userRepository;

    @Test
    void signupEndpointIsPublicAndRoleIsParticipant() throws Exception {
        User created = new User();
        created.setId(1L);
        created.setUsername("participant");
        created.setEmail("participant@example.com");
        created.setRole("PARTICIPANT");
        when(userRegistrationService.registerParticipant(any(RegistrationRequest.class))).thenReturn(created);

        String payload = """
            {
              "username":"participant",
              "email":"participant@example.com",
              "password":"password123",
              "displayName":"Participant Name",
              "role":"ADMIN"
            }
            """;

        mockMvc.perform(post("/signup")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.role").value("PARTICIPANT"));

        ArgumentCaptor<RegistrationRequest> captor = ArgumentCaptor.forClass(RegistrationRequest.class);
        verify(userRegistrationService).registerParticipant(captor.capture());
        RegistrationRequest captured = captor.getValue();
        assertEquals("participant", captured.getUsername());
        assertEquals("participant@example.com", captured.getEmail());
        assertEquals("Participant Name", captured.getDisplayName());
    }

    @Test
    void adminCreationRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/admin/admins")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPayload()))
            .andExpect(status().isFound())
            .andExpect(header().string("Location", "http://localhost/login"));
    }

    @Test
    @WithMockUser(roles = "PARTICIPANT")
    void adminCreationRequiresAdminRole() throws Exception {
        mockMvc.perform(post("/admin/admins")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPayload()))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanCreateAdmin() throws Exception {
        User created = new User();
        created.setId(2L);
        created.setUsername("admin");
        created.setEmail("admin@example.com");
        created.setRole("ADMIN");
        when(userRegistrationService.registerAdmin(any(RegistrationRequest.class))).thenReturn(created);

        mockMvc.perform(post("/admin/admins")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPayload()))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCreationRequiresCsrfToken() throws Exception {
        mockMvc.perform(post("/admin/admins")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPayload()))
            .andExpect(status().isForbidden());
    }

    private String validPayload() throws Exception {
        RegistrationRequest request = new RegistrationRequest();
        request.setUsername("admin-user");
        request.setEmail("admin@example.com");
        request.setPassword("password123");
        request.setDisplayName("Admin User");
        return objectMapper.writeValueAsString(request);
    }
}
