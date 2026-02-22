package com.fintech.backend.controller;

import com.fintech.backend.model.User;
import com.fintech.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository,
                          BCryptPasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public static class LoginRequest {
        public String email;
        public String password;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        if (req == null || req.email == null || req.password == null) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "Email және құпия сөз қажет", "code", "BAD_REQUEST"));
        }

        return userRepository.findByEmail(req.email.trim().toLowerCase())
                .map(user -> {
                    if (!passwordEncoder.matches(req.password, user.getPasswordHash())) {
                        // Не раскрываем, что именно не так — безопасность
                        return ResponseEntity.status(401)
                                .body(Map.of("message", "Қате email немесе құпия сөз", "code", "INVALID_CREDENTIALS"));
                    }
                    String token = jwtUtil.generateToken(user);
                    long expiresIn = jwtUtil.getExpirationMs();
                    Date expiresAt = new Date(System.currentTimeMillis() + expiresIn);
                    return ResponseEntity.ok(Map.of(
                            "token", token,
                            "tokenType", "Bearer",
                            "expiresIn", expiresIn,
                            "expiresAt", expiresAt.toString()
                    ));
                })
                .orElseGet(() -> ResponseEntity.status(401)
                        .body(Map.of("message", "Қате email немесе құпия сөз", "code", "INVALID_CREDENTIALS")));
    }

}
