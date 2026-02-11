package com.cultureSL.CultureLog.service;

import com.cultureSL.CultureLog.model.User;
import java.util.Optional;

public interface UserService {
    
    User registerUser(User user) throws Exception;
    
    Optional<User> login(String username, String rawPassword);
    
    boolean exists(String username);

    void updateProfilePicture(Long userId, String imageUrl);

    void removeProfilePicture(Long userId);

    void requestPasswordReset(String email) throws Exception;

    void resetPassword(String token, String newPassword) throws Exception;
}