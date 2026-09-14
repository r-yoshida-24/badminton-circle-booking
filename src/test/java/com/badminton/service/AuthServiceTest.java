package com.badminton.service;

import com.badminton.entity.User;
import com.badminton.entity.UserRole;
import com.badminton.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerParticipantAlwaysSetsParticipantRole() {
        when(userRepository.findByUsername("participant")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("participant@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.registerParticipant("participant", "password", "participant@example.com", "Participant");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(UserRole.PARTICIPANT);
        assertThat(captor.getValue().getPassword()).isEqualTo("encoded-password");
    }
}
