package com.fintech.backend;

import com.fintech.backend.controller.JwtUtil;
import com.fintech.backend.controller.UserController;
import com.fintech.backend.model.User;
import com.fintech.backend.repository.InvestorRepository;
import com.fintech.backend.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserRepository repo;

    @Mock
    private InvestorRepository investorRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserController controller;

    @Test
    void register_success() {
        UserController.CreateUserRequest req = new UserController.CreateUserRequest();
        req.email = "test@mail.com";
        req.password = "12345678";

        when(repo.existsByEmail(any())).thenReturn(false);

        User saved = new User();
        saved.setId("1");

        when(repo.save(any())).thenReturn(saved);

        ResponseEntity<?> response = controller.register(req);

        assertEquals(201, response.getStatusCodeValue());
        verify(repo, times(1)).save(any());
    }

    @Test
    void register_email_exists() {
        UserController.CreateUserRequest req = new UserController.CreateUserRequest();
        req.email = "test@mail.com";
        req.password = "12345678";

        when(repo.existsByEmail(any())).thenReturn(true);

        ResponseEntity<?> response = controller.register(req);

        assertEquals(409, response.getStatusCodeValue());
        verify(repo, never()).save(any());
    }

    @Test
    void register_invalid_email() {
        UserController.CreateUserRequest req = new UserController.CreateUserRequest();
        req.email = "invalid-email";
        req.password = "12345678";

        ResponseEntity<?> response = controller.register(req);

        assertEquals(400, response.getStatusCodeValue());
    }

    @Test
    void register_weak_password() {
        UserController.CreateUserRequest req = new UserController.CreateUserRequest();
        req.email = "test@mail.com";
        req.password = "123";

        ResponseEntity<?> response = controller.register(req);

        assertEquals(400, response.getStatusCodeValue());
    }

    @Test
    void getById_found() {
        User user = new User();
        user.setId("1");

        when(repo.findById("1")).thenReturn(Optional.of(user));

        ResponseEntity<?> response = controller.getById("1");

        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void getById_not_found() {
        when(repo.findById("1")).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.getById("1");

        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void me_success() {
        String token = "valid-token";

        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.getUserIdFromToken(token)).thenReturn("1");

        User user = new User();
        user.setId("1");

        when(repo.findById("1")).thenReturn(Optional.of(user));

        ResponseEntity<?> response = controller.me("Bearer " + token);

        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void me_no_header() {
        ResponseEntity<?> response = controller.me(null);

        assertEquals(401, response.getStatusCodeValue());
    }

    @Test
    void me_invalid_token() {
        when(jwtUtil.validateToken("bad")).thenReturn(false);

        ResponseEntity<?> response = controller.me("Bearer bad");

        assertEquals(401, response.getStatusCodeValue());
    }

    @Test
    void me_user_not_found() {
        when(jwtUtil.validateToken("token")).thenReturn(true);
        when(jwtUtil.getUserIdFromToken("token")).thenReturn("1");
        when(repo.findById("1")).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.me("Bearer token");

        assertEquals(401, response.getStatusCodeValue());
    }
}