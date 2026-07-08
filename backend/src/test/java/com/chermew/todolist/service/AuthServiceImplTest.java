package com.chermew.todolist.service;

import com.chermew.todolist.dto.AuthResponse;
import com.chermew.todolist.dto.LoginRequest;
import com.chermew.todolist.dto.RegisterRequest;
import com.chermew.todolist.entity.User;
import com.chermew.todolist.enums.UserStatus;
import com.chermew.todolist.repository.UserRepository;
import com.chermew.todolist.security.JwtTokenProvider;
import com.chermew.todolist.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = User.builder()
                .id(userId)
                .email("test@example.com")
                .passwordHash("hashedPassword")
                .fullName("Test User")
                .status(UserStatus.ACTIVE)
                .build();
    }

    @Test
    void register_Success() {
        RegisterRequest request = RegisterRequest.builder()
                .email("test@example.com")
                .password("password")
                .fullName("Test User")
                .build();

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(tokenProvider.generateToken("test@example.com")).thenReturn("testToken");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("testToken", response.getAccessToken());
        assertEquals(userId, response.getUserId());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("Test User", response.getFullName());
    }

    @Test
    void register_EmailAlreadyExists_ThrowsException() {
        RegisterRequest request = RegisterRequest.builder()
                .email("test@example.com")
                .password("password")
                .build();

        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> authService.register(request));
    }

    @Test
    void login_Success() {
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("password")
                .build();

        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("testToken");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("testToken", response.getAccessToken());
        assertEquals(userId, response.getUserId());
        assertEquals("test@example.com", response.getEmail());
    }

    @Test
    void login_UserNotFound_ThrowsException() {
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("password")
                .build();

        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("testToken");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> authService.login(request));
    }
}
