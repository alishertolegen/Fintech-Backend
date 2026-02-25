package com.fintech.backend.controller;

import com.fintech.backend.model.User;
import com.fintech.backend.model.Investor;
import com.fintech.backend.repository.UserRepository;
import com.fintech.backend.repository.InvestorRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository repo;
    private final InvestorRepository investorRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final JwtUtil jwtUtil; // <-- добавлено
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    public UserController(UserRepository repo, JwtUtil jwtUtil, InvestorRepository investorRepository) {
        this.repo = repo;
        this.jwtUtil = jwtUtil; // <-- назначаем
        this.investorRepository = investorRepository;
    }


    // ---- DTOs (вложенные, чтобы не создавать доп. файлов) ----
    public static class CreateUserRequest {
        public String email;
        public String password;
        public String role; // optional
        public String name;
        public String company;
        public String bio;
        public String avatarUrl;
        public String phone;
        public String location;

        // вложенный профиль инвестора (опционально)
        public InvestorProfile investorProfile;

        public static class InvestorProfile {
            public String legalName;
            public String type;
            public Integer minCheck;
            public Integer maxCheck;
            public List<String> preferredIndustries;
            public List<String> preferredStages;
            public String website;
        }
    }

    public static class UpdateUserRequest {
        public String name;
        public String company;
        public String bio;
        public String avatarUrl;
        public Boolean isVerified;
        public String phone;
        public String location;
    }

    // ---- Helpers ----
    private User sanitize(User u) {
        if (u == null) return null;
        u.setPasswordHash(null);
        return u;
    }

    // ---- Endpoints ----

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody CreateUserRequest req) {
        log.info("Register attempt for email: {}", req.email);
        if (req == null || req.email == null || req.password == null) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "Email және құпия сөз қажет", "code", "BAD_REQUEST"));
        }

        String email = req.email.trim().toLowerCase();

        // Простейшая валидация email
        Pattern emailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
        if (!emailPattern.matcher(email).matches()) {
            log.warn("Invalid email format: {}", email);
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "Электрондық пошта форматы дұрыс емес", "code", "INVALID_EMAIL"));
        }

        // Минимальные требования к паролю
        if (req.password.length() < 8) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "Құпия сөз кемінде 8 таңбадан тұруы керек", "code", "WEAK_PASSWORD"));
        }

        if (repo.existsByEmail(email)) {
            log.warn("Email already exists: {}", email);
            return ResponseEntity
                    .status(409)
                    .body(Map.of("message", "Бұл email бұрыннан тіркелген", "code", "EMAIL_EXISTS"));
        }

        String role = (req.role == null || req.role.isBlank()) ? "founder" : req.role;
        String hashed = encoder.encode(req.password);

        User u = new User();
        u.setEmail(email);
        u.setPasswordHash(hashed);
        u.setRole(role);
        u.setName(req.name);
        u.setCompany(req.company);
        u.setBio(req.bio);
        u.setAvatarUrl(req.avatarUrl);
        u.setCreatedAt(Instant.now());
        u.setUpdatedAt(u.getCreatedAt());
        u.setIsVerified(false);
        u.setMeta(new User.Meta(req.phone, req.location));

        try {
            User saved = repo.save(u);

            // Если роль investor и пришёл профиль — пытаемся создать Investor
            if ("investor".equalsIgnoreCase(role) && req.investorProfile != null) {
                try {
                    Investor inv = new Investor();
                    inv.setUserId(saved.getId());
                    inv.setLegalName(
                            (req.investorProfile.legalName == null || req.investorProfile.legalName.isBlank())
                                    ? saved.getName()
                                    : req.investorProfile.legalName
                    );
                    inv.setType(req.investorProfile.type);
                    inv.setMinCheck(req.investorProfile.minCheck);
                    inv.setMaxCheck(req.investorProfile.maxCheck);
                    inv.setPreferredIndustries(req.investorProfile.preferredIndustries);
                    inv.setPreferredStages(req.investorProfile.preferredStages);
                    inv.setWebsite(req.investorProfile.website);
                    inv.setCreatedAt(Instant.now());
                    inv.setUpdatedAt(Instant.now());
                    Investor savedInv = investorRepository.save(inv);
                    log.info("User registered successfully: id={}, email={}", saved.getId(), email);
                    // Возвращаем единый объект: message + user + investor
                    return ResponseEntity
                            .status(201)
                            .body(Map.of(
                                    "message", "Тіркелу сәтті өтті",
                                    "code", "REGISTERED",
                                    "user", sanitize(saved),
                                    "investor", savedInv
                            ));
                } catch (Exception invEx) {
                    // Если при создании профиля инвестора что-то пошло не так — откатываем user
                    invEx.printStackTrace();
                    try {
                        repo.deleteById(saved.getId());
                    } catch (Exception delEx) {
                        delEx.printStackTrace();
                        // Если откат не удался — сообщаем об ошибке, но возвращаем 500
                        return ResponseEntity
                                .status(500)
                                .body(Map.of("message", "Пайдаланушы сақталды, бірақ инвестор профилін жасау сәтсіз аяқталды. Роллбек сәтсіз.", "code", "INVESTOR_CREATE_ROLLBACK_FAILED"));
                    }
                    log.error("Investor profile creation failed for userId={}", saved.getId(), invEx);
                    return ResponseEntity
                            .status(500)
                            .body(Map.of("message", "Инвестор профилін жасау кезінде қате орын алды", "code", "INVESTOR_CREATE_FAILED"));
                }
            }

            // Без инвестор-профиля — обычный ответ
            log.info("User registered successfully: id={}, email={}", saved.getId(), email);
            return ResponseEntity
                    .status(201)
                    .body(Map.of("message", "Тіркелу сәтті өтті", "code", "REGISTERED", "user", sanitize(saved)));
        } catch (DataIntegrityViolationException ex) {
            // На случай гонки/уникальности в БД
            return ResponseEntity
                    .status(409)
                    .body(Map.of("message", "Бұл email бұрыннан тіркелген", "code", "EMAIL_EXISTS"));
        } catch (Exception ex) {
            // Логируем на сервере (System.err или логгер)
            ex.printStackTrace();
            log.error("Server error during registration", ex);
            return ResponseEntity
                    .status(500)
                    .body(Map.of("message", "Серверде қате орын алды", "code", "SERVER_ERROR"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        log.info("Fetching user by id={}", id);
        return repo.findById(id)
                .map(u -> ResponseEntity.ok(sanitize(u)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<User>> list(@RequestParam(required = false) String role) {
        List<User> list;
        if (role == null || role.isBlank()) {
            list = repo.findAll();
        } else {
            list = repo.findAllByRole(role);
        }
        List<User> sanitized = list.stream().map(this::sanitize).collect(Collectors.toList());
        return ResponseEntity.ok(sanitized);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody UpdateUserRequest req) {
        log.info("Updating user id={}", id);
        return repo.findById(id).map(u -> {
            if (req.name != null) u.setName(req.name);
            if (req.company != null) u.setCompany(req.company);
            if (req.bio != null) u.setBio(req.bio);
            if (req.avatarUrl != null) u.setAvatarUrl(req.avatarUrl);
            if (req.isVerified != null) u.setIsVerified(req.isVerified);
            if (u.getMeta() == null) u.setMeta(new User.Meta(req.phone, req.location));
            else {
                if (req.phone != null) u.getMeta().setPhone(req.phone);
                if (req.location != null) u.getMeta().setLocation(req.location);
            }
            u.setUpdatedAt(Instant.now());
            User saved = repo.save(u);
            return ResponseEntity.ok(sanitize(saved));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        log.info("Deleting user id={}", id);
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ---- New: current user ----
    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader(name = "Authorization", required = false) String authHeader) {
        log.info("Fetching current user from token");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401)
                    .body(Map.of("message", "Authorization header жетіспейді немесе дұрыс емес", "code", "MISSING_AUTH"));
        }
        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401)
                    .body(Map.of("message", "Токен жарамсыз немесе мерзімі өткен", "code", "INVALID_TOKEN"));
        }
        String userId = jwtUtil.getUserIdFromToken(token);

        var opt = repo.findById(userId);
        if (opt.isEmpty()) {
            return ResponseEntity.status(401)
                    .body(Map.of("message", "Пайдаланушы табылған жоқ", "code", "USER_NOT_FOUND"));
        }
        User u = opt.get();
        return ResponseEntity.ok(sanitize(u));
    }


}