package com.moonlight.stays.features.auth;

import com.moonlight.stays.features.auth.dto.AuthResponse;
import com.moonlight.stays.features.auth.dto.LoginRequest;
import com.moonlight.stays.features.auth.dto.RegistrationRequest;
import com.moonlight.stays.features.auth.model.UserRole;
import com.moonlight.stays.features.auth.service.AuthService;
import com.moonlight.stays.features.user.model.User;
import com.moonlight.stays.features.user.repository.UserRepository;
import com.moonlight.stays.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("test@moonlight.com")
                .password("encoded_pass")
                .firstName("Snehil")
                .lastName("Kumar")
                .role(UserRole.ROLE_GUEST)
                .build();
    }

    @Test
    void registerUser_Success() {
        RegistrationRequest request = new RegistrationRequest();
        request.setEmail("test@moonlight.com");
        request.setPassword("SecurePass123!");
        request.setFirstName("Snehil");
        request.setLastName("Kumar");
        request.setRole(UserRole.ROLE_GUEST);

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_pass");
        when(jwtTokenProvider.generateToken(any(User.class))).thenReturn("access_token");
        when(jwtTokenProvider.generateRefreshToken(any(User.class))).thenReturn("refresh_token");

        AuthResponse response = authService.registerUser(request);

        assertNotNull(response);
        assertEquals("access_token", response.getAccessToken());
        assertEquals("test@moonlight.com", response.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void authenticateUser_Success() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@moonlight.com");
        request.setPassword("SecurePass123!");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtTokenProvider.generateToken(any(User.class))).thenReturn("access_token");
        when(jwtTokenProvider.generateRefreshToken(any(User.class))).thenReturn("refresh_token");

        AuthResponse response = authService.authenticateUser(request);

        assertNotNull(response);
        assertEquals("access_token", response.getAccessToken());
        verify(userRepository, times(1)).findByEmail("test@moonlight.com");
    }
}
