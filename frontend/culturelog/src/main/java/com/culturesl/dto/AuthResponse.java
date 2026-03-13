package com.culturesl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private Long id;
    private String username;
    private String email;
    private String token;
    private String message;
}