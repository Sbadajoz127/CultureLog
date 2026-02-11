package com.cultureSL.CultureLog.service;

public interface EmailService {
    
    void sendHtmlEmail(String to, String subject, String htmlBody);

    void sendNewFollowerNotification(String toEmail, String followerUsername);

    void sendPasswordResetEmail(String to, String token);
}