package com.badminton.controller;

import com.badminton.TestPrincipalFactory;
import com.badminton.dto.LineAuthRequest;
import com.badminton.entity.Member;
import com.badminton.entity.Role;
import com.badminton.repository.MemberRepository;
import com.badminton.security.LineIdTokenClaims;
import com.badminton.security.LineIdTokenVerifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthAndSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MemberRepository memberRepository;

    @MockitoBean
    private LineIdTokenVerifier lineIdTokenVerifier;

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();
    }

    @Test
    void lineAuthenticationCreatesSessionForRegisteredMember() throws Exception {
        Member member = saveMember("U123", Role.USER, true);
        when(lineIdTokenVerifier.verify("valid-token")).thenReturn(new LineIdTokenClaims("U123", "会員A"));

        MockHttpSession session = (MockHttpSession) mockMvc.perform(post("/auth/line")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LineAuthRequest("valid-token", true))))
                .andExpect(status().isOk())
                .andReturn()
                .getRequest()
                .getSession(false);

        mockMvc.perform(get("/attendance").session(session))
                .andExpect(status().isOk())
                .andExpect(authenticated().withUsername(member.getLineUserId()));
    }

    @Test
    void unregisteredMemberIsRejected() throws Exception {
        when(lineIdTokenVerifier.verify("valid-token")).thenReturn(new LineIdTokenClaims("U404", "未登録"));

        mockMvc.perform(post("/auth/line")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LineAuthRequest("valid-token", true))))
                .andExpect(status().isForbidden());
    }

    @Test
    void disabledMemberIsRejected() throws Exception {
        saveMember("U999", Role.USER, false);
        when(lineIdTokenVerifier.verify("valid-token")).thenReturn(new LineIdTokenClaims("U999", "停止会員"));

        mockMvc.perform(post("/auth/line")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LineAuthRequest("valid-token", true))))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedAccessToAttendanceRedirectsToLiffLogin() throws Exception {
        mockMvc.perform(get("/attendance"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/liff-login"));
    }

    @Test
    void adminCanAccessAdminPage() throws Exception {
        Member member = saveMember("UADMIN", Role.ADMIN, true);

        mockMvc.perform(get("/admin/events")
                        .with(TestPrincipalFactory.authentication(TestPrincipalFactory.principal(member.getId(), member.getLineUserId(), member.getDisplayName(), member.getRole()))))
                .andExpect(status().isOk());
    }

    @Test
    void userCannotAccessAdminPage() throws Exception {
        Member member = saveMember("UUSER", Role.USER, true);

        mockMvc.perform(get("/admin/events")
                        .with(TestPrincipalFactory.authentication(TestPrincipalFactory.principal(member.getId(), member.getLineUserId(), member.getDisplayName(), member.getRole()))))
                .andExpect(status().isForbidden());
    }

    private Member saveMember(String lineUserId, Role role, boolean enabled) {
        Member member = new Member();
        member.setLineUserId(lineUserId);
        member.setDisplayName(lineUserId);
        member.setRole(role);
        member.setEnabled(enabled);
        return memberRepository.save(member);
    }
}
