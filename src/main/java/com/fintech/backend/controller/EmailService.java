// controller/EmailService.java
package com.fintech.backend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordResetEmail(String toEmail, String token) {
        String resetLink = frontendUrl + "/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Құпия сөзді қалпына келтіру");
        message.setText(
                "Сәлем!\n\n" +
                        "Құпия сөзді қалпына келтіру үшін төмендегі сілтемені басыңыз:\n\n" +
                        resetLink + "\n\n" +
                        "Сілтеме 30 минут бойы жарамды.\n\n" +
                        "Егер сіз бұл сұранысты жібермесеңіз — хабарламаны елемеңіз."
        );

        mailSender.send(message);
    }
}