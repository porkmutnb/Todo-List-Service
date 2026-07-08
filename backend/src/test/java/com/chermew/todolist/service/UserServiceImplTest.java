package com.chermew.todolist.service;

import com.chermew.todolist.dto.UpdateProfileRequest;
import com.chermew.todolist.dto.UserProfileResponse;
import com.chermew.todolist.entity.User;
import com.chermew.todolist.enums.UserGender;
import com.chermew.todolist.enums.UserStatus;
import com.chermew.todolist.repository.UserRepository;
import com.chermew.todolist.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private UUID userId;
    private User user;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = User.builder()
                .id(userId)
                .email("user@example.com")
                .passwordHash("hashedPassword")
                .fullName("Original Name")
                .status(UserStatus.ACTIVE)
                .build();
    }

    @Test
    void updateProfile_Success_WithBioAndAvatar() {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .fullName("Updated Name")
                .email("user@example.com")
                .bio(UserGender.MALE)
                .avatarUrl("base64data")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserProfileResponse response = userService.updateProfile(userId, request);

        assertNotNull(response);
        assertEquals("Updated Name", user.getFullName());
        assertEquals(UserGender.MALE, user.getBio());
        assertEquals("base64data", user.getAvatarUrl());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateProfile_Success_WithPassword() {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .password("oldPassword")
                .newPassword("newPassword")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("oldPassword")).thenReturn("hashedPassword");
        when(passwordEncoder.encode("newPassword")).thenReturn("newHashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserProfileResponse response = userService.updateProfile(userId, request);

        assertNotNull(response);
        assertEquals("newHashedPassword", user.getPasswordHash());
    }

    @Test
    void updateProfile_InvalidPassword_ThrowsException() {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .password("wrongPassword")
                .newPassword("newPassword")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("wrongPassword")).thenReturn("wrongHashedPassword");

        assertThrows(IllegalArgumentException.class, () -> userService.updateProfile(userId, request));
    }

    @Test
    void updateProfile_UserNotFound_ThrowsException() {
        UpdateProfileRequest request = UpdateProfileRequest.builder().fullName("Updated").build();
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userService.updateProfile(UUID.randomUUID(), request));
    }

    @Test
    void getProfile_Success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserProfileResponse response = userService.getProfile(userId);

        assertNotNull(response);
        assertEquals(userId, response.getId());
    }

    @Test
    void getProfile_UserNotFound_ThrowsException() {
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userService.getProfile(UUID.randomUUID()));
    }
}
