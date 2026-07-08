package com.chermew.todolist.service;

import com.chermew.todolist.dto.UpdateProfileRequest;
import com.chermew.todolist.dto.UserProfileResponse;

import java.util.UUID;

public interface UserService {
    UserProfileResponse getProfile(UUID userId);
    UserProfileResponse updateProfile(UUID userId, UpdateProfileRequest request);
}
