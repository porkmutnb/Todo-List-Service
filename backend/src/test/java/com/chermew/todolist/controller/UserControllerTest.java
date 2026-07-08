package com.chermew.todolist.controller;

import com.chermew.todolist.dto.UpdateProfileRequest;
import com.chermew.todolist.dto.UserProfileResponse;
import com.chermew.todolist.enums.UserGender;
import com.chermew.todolist.enums.UserStatus;
import com.chermew.todolist.security.UserPrincipal;
import com.chermew.todolist.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private UUID mockUserId;
    private UserPrincipal userPrincipal;
    private UserProfileResponse profileResponse;

    private final HandlerMethodArgumentResolver authenticationPrincipalResolver = new HandlerMethodArgumentResolver() {
        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.getParameterType().equals(UserPrincipal.class);
        }

        @Override
        public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                      NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
            return userPrincipal;
        }
    };

    @BeforeEach
    void setUp() {
        mockUserId = UUID.randomUUID();
        userPrincipal = UserPrincipal.builder()
                .id(mockUserId)
                .email("user@example.com")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        profileResponse = UserProfileResponse.builder()
                .id(mockUserId)
                .email("user@example.com")
                .fullName("User Name")
                .status(UserStatus.ACTIVE)
                .bio(UserGender.MALE)
                .avatarUrl("base64data")
                .build();

        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setCustomArgumentResolvers(authenticationPrincipalResolver)
                .setControllerAdvice(new com.chermew.todolist.exception.GlobalExceptionHandler())
                .build();
    }

    @Test
    void getProfile_Success() throws Exception {
        when(userService.getProfile(mockUserId)).thenReturn(profileResponse);

        mockMvc.perform(get("/api/v1/users/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fullName").value("User Name"))
                .andExpect(jsonPath("$.data.bio").value("male"))
                .andExpect(jsonPath("$.data.avatarUrl").value("base64data"));
    }

    @Test
    void getUser_Success() throws Exception {
        when(userService.getProfile(mockUserId)).thenReturn(profileResponse);

        mockMvc.perform(get("/api/v1/users/" + mockUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fullName").value("User Name"))
                .andExpect(jsonPath("$.data.bio").value("male"))
                .andExpect(jsonPath("$.data.avatarUrl").value("base64data"));
    }

    @Test
    void updateProfile_Success() throws Exception {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .fullName("New Name")
                .bio(UserGender.FEMALE)
                .avatarUrl("newbase64data")
                .build();

        profileResponse.setFullName("New Name");
        profileResponse.setEmail("user@example.com");
        profileResponse.setBio(UserGender.FEMALE);
        profileResponse.setAvatarUrl("newbase64data");

        when(userService.updateProfile(eq(mockUserId), any(UpdateProfileRequest.class))).thenReturn(profileResponse);

        mockMvc.perform(put("/api/v1/users/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fullName").value("New Name"))
                .andExpect(jsonPath("$.data.bio").value("female"))
                .andExpect(jsonPath("$.data.avatarUrl").value("newbase64data"));
    }

    @Test
    void getUser_NotFound() throws Exception {
        when(userService.getProfile(mockUserId)).thenThrow(new IllegalArgumentException("User not found with id: " + mockUserId));
        mockMvc.perform(get("/api/v1/users/" + mockUserId))
                .andExpect(status().isNotFound());
    }
}
