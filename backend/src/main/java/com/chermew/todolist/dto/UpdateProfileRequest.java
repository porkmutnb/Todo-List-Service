package com.chermew.todolist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.chermew.todolist.enums.UserGender;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {
    private String fullName;
    private String email;
    private String password;
    private String newPassword;
    private UserGender bio;
    private String avatarUrl;
}
