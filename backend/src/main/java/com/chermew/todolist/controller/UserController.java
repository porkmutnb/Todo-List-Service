package com.chermew.todolist.controller;

import com.chermew.todolist.annotation.LogActivity;
import com.chermew.todolist.dto.ApiResponse;
import com.chermew.todolist.dto.UpdateProfileRequest;
import com.chermew.todolist.dto.UserProfileResponse;
import com.chermew.todolist.security.UserPrincipal;
import com.chermew.todolist.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    @LogActivity(action = "USER_PROFILE_GET", entityType = "users")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        UserProfileResponse response = userService.getProfile(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Profile fetched successfully", response));
    }

    @GetMapping("/{userId}")
    @LogActivity(action = "GET_USER", entityType = "users")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(@PathVariable UUID userId) {
        UserProfileResponse response = userService.getProfile(userId);
        return ResponseEntity.ok(ApiResponse.success("User fetched successfully", response));
    }

    @PutMapping("/profile")
    @LogActivity(action = "USER_PROFILE_UPDATE", entityType = "users")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody UpdateProfileRequest updateProfileRequest) {
        UserProfileResponse response = userService.updateProfile(userPrincipal.getId(), updateProfileRequest);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", response));
    }
}
