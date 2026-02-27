// controller/PasswordResetController.java
package com.fintech.backend.controller;

import com.fintech.backend.model.PasswordResetToken;
import com.fintech.backend.model.User;
import com.fintech.backend.repository.PasswordResetTokenRepository;
import com.fintech.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class PasswordResetController {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetController.class);

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public PasswordResetController(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            EmailService emailService) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
    }

    // DTO
    public static class ForgotPasswordRequest {
        public String email;
    }

    public static class ResetPasswordRequest {
        public String token;
        public String newPassword;
    }

    /**
     * POST /api/auth/forgot-password
     * Принимает email, генерирует токен и отправляет письмо.
     * Всегда возвращает 200 (чтобы не раскрывать, есть ли такой email).
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest req) {
        if (req == null || req.email == null || req.email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Email қажет",
                    "code", "BAD_REQUEST"
            ));
        }

        String email = req.email.trim().toLowerCase();
        log.info("Password reset requested for email={}", email);

        Optional<User> maybeUser = userRepository.findByEmail(email);

        if (maybeUser.isPresent()) {
            User user = maybeUser.get();

            // Удаляем старые токены этого юзера
            tokenRepository.deleteByUserId(user.getId());

            String rawToken = UUID.randomUUID().toString();
            Instant expiresAt = Instant.now().plusSeconds(30 * 60); // 30 минут

            PasswordResetToken resetToken = new PasswordResetToken(user.getId(), rawToken, expiresAt);
            tokenRepository.save(resetToken);

            try {
                emailService.sendPasswordResetEmail(email, rawToken);
                log.info("Password reset email sent to email={}", email);
            } catch (Exception ex) {
                log.error("Failed to send reset email to email={}", email, ex);
                // Не раскрываем детали ошибки клиенту
            }
        }

        // Всегда одинаковый ответ
        return ResponseEntity.ok(Map.of(
                "message", "Егер бұл email тіркелген болса, хат жіберілді",
                "code", "RESET_EMAIL_SENT"
        ));
    }

    /**
     * POST /api/auth/reset-password
     * Принимает токен и новый пароль, меняет пароль.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest req) {
        if (req == null || req.token == null || req.newPassword == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Токен және жаңа құпия сөз қажет",
                    "code", "BAD_REQUEST"
            ));
        }

        if (req.newPassword.length() < 8) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Құпия сөз кемінде 8 таңбадан тұруы керек",
                    "code", "WEAK_PASSWORD"
            ));
        }

        Optional<PasswordResetToken> maybeToken = tokenRepository.findByToken(req.token);

        if (maybeToken.isEmpty()) {
            return ResponseEntity.status(400).body(Map.of(
                    "message", "Токен жарамсыз",
                    "code", "INVALID_TOKEN"
            ));
        }

        PasswordResetToken resetToken = maybeToken.get();

        if (resetToken.isUsed()) {
            return ResponseEntity.status(400).body(Map.of(
                    "message", "Токен бұрын қолданылған",
                    "code", "TOKEN_ALREADY_USED"
            ));
        }

        if (Instant.now().isAfter(resetToken.getExpiresAt())) {
            tokenRepository.delete(resetToken);
            return ResponseEntity.status(400).body(Map.of(
                    "message", "Токеннің мерзімі өткен",
                    "code", "TOKEN_EXPIRED"
            ));
        }

        Optional<User> maybeUser = userRepository.findById(resetToken.getUserId());
        if (maybeUser.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of(
                    "message", "Пайдаланушы табылмады",
                    "code", "USER_NOT_FOUND"
            ));
        }

        User user = maybeUser.get();
        user.setPasswordHash(encoder.encode(req.newPassword));
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

        // Инвалидируем токен
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        log.info("Password reset successful for userId={}", user.getId());

        return ResponseEntity.ok(Map.of(
                "message", "Құпия сөз сәтті өзгертілді",
                "code", "PASSWORD_RESET_SUCCESS"
        ));
    }
}