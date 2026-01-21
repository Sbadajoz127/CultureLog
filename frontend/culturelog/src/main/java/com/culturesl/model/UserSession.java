package com.culturesl.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSession {
    private static UserSession instance;

    private Long userId;
    
    private String username;
    
    private String email;

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void cleanSession() {
        userId = null;
        username = null;
        email = null;
    }
    
    public boolean isLoggedIn() {
        return userId != null;
    }
}