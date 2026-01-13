package com.culturesl.api;

import com.culturesl.dto.AuthResponse;
import com.culturesl.dto.LoginRequest;
import com.culturesl.dto.RegisterRequest;

public class AuthService {

    private final ApiClient apiClient;

    public AuthService() {
        this.apiClient = new ApiClient();
    }

    public AuthResponse login(String username, String password) throws Exception {
        LoginRequest request = new LoginRequest(username, password);
                
        return apiClient.post("/auth/login", request, AuthResponse.class);
    }

    public AuthResponse register(String username, String password, String email) throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username(username)
                .password(password)
                .email(email)
                .build();

        return apiClient.post("/auth/register", request, AuthResponse.class);
    }
}