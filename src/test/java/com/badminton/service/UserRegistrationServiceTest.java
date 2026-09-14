package com.badminton.service;

import com.badminton.dto.RegistrationRequest;
import com.badminton.entity.User;
import com.badminton.exception.DuplicateFieldException;
import com.badminton.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRegistrationServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserRegistrationService userRegistrationService;

    @Test
    void registerParticipantAssignsParticipantRoleAndEncodedPassword() {
        RegistrationRequest request = request("participant", "participant@example.com");
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User created = userRegistrationService.registerParticipant(request);

        assertEquals("PARTICIPANT", created.getRole());
        assertEquals("encoded-password", created.getPassword());
    }

    @Test
    void registerAdminAssignsAdminRole() {
        RegistrationRequest request = request("admin-user", "admin@example.com");
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User created = userRegistrationService.registerAdmin(request);

        assertEquals("ADMIN", created.getRole());
        assertEquals("encoded-password", created.getPassword());
    }

    @Test
    void duplicateUsernameThrowsConflictError() {
        RegistrationRequest request = request("dup-user", "user@example.com");
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(true);

        DuplicateFieldException exception = assertThrows(
            DuplicateFieldException.class,
            () -> userRegistrationService.registerParticipant(request)
        );

        assertEquals("username", exception.getField());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void duplicateEmailThrowsConflictError() {
        RegistrationRequest request = request("user", "dup@example.com");
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        DuplicateFieldException exception = assertThrows(
            DuplicateFieldException.class,
            () -> userRegistrationService.registerAdmin(request)
        );

        assertEquals("email", exception.getField());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void persistedUserDoesNotUsePlainPassword() {
        RegistrationRequest request = request("plain-check", "plain-check@example.com");
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userRegistrationService.registerParticipant(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertNotEquals(request.getPassword(), captor.getValue().getPassword());
    }

    @Test
    void saveConstraintViolationIsTranslatedToDuplicateFieldError() {
        RegistrationRequest request = request("dup-user", "dup@example.com");
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded");
        when(userRepository.save(any(User.class)))
            .thenThrow(new DataIntegrityViolationException("duplicate key value violates unique constraint users_username_key"));

        DuplicateFieldException exception = assertThrows(
            DuplicateFieldException.class,
            () -> userRegistrationService.registerParticipant(request)
        );

        assertEquals("username", exception.getField());
    }

    private RegistrationRequest request(String username, String email) {
        RegistrationRequest request = new RegistrationRequest();
        request.setUsername(username);
        request.setEmail(email);
        request.setPassword("password123");
        request.setDisplayName("displayName");
        return request;
    }
}
