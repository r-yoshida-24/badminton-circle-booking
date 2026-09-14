package com.badminton.controller;

import com.badminton.config.SecurityConfig;
import com.badminton.security.RoleBasedAuthenticationFailureHandler;
import com.badminton.security.RoleBasedAuthenticationSuccessHandler;
import com.badminton.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AuthController.class, AuthPageController.class})
@Import({SecurityConfig.class, RoleBasedAuthenticationSuccessHandler.class, RoleBasedAuthenticationFailureHandler.class})
class AuthControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Test
    void participantRegisterPageIsPublic() throws Exception {
        mockMvc.perform(get("/participant/register"))
            .andExpect(status().isOk());
    }

    @Test
    void participantRegisterUsesParticipantFlow() throws Exception {
        mockMvc.perform(post("/participant/register")
                .with(csrf())
                .param("username", "u")
                .param("password", "p")
                .param("email", "u@example.com")
                .param("displayName", "User")
                .param("role", "ADMIN"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/participant/login?registered"));

        verify(authService).registerParticipant("u", "p", "u@example.com", "User");
        verifyNoMoreInteractions(authService);
    }

    @Test
    @WithMockUser(roles = "PARTICIPANT")
    void participantCannotAccessAdminRegisterEndpoint() throws Exception {
        mockMvc.perform(post("/admin/register")
                .with(csrf())
                .param("username", "admin")
                .param("password", "p")
                .param("email", "admin@example.com")
                .param("displayName", "Admin"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanAccessAdminRegisterEndpoint() throws Exception {
        mockMvc.perform(post("/admin/register")
                .with(csrf())
                .param("username", "admin")
                .param("password", "p")
                .param("email", "admin@example.com")
                .param("displayName", "Admin"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/login?registered"));

        verify(authService).registerAdmin("admin", "p", "admin@example.com", "Admin");
    }
}
