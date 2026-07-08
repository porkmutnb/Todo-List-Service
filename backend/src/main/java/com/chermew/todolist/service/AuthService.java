package com.chermew.todolist.service;

import com.chermew.todolist.dto.AuthResponse;
import com.chermew.todolist.dto.LoginRequest;
import com.chermew.todolist.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest registerRequest);
    AuthResponse login(LoginRequest loginRequest);
}
