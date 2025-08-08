package efive.tempodoro.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import efive.tempodoro.dto.LoginRequest;
import efive.tempodoro.dto.LoginResponse;
import efive.tempodoro.dto.RegisterRequest;
import efive.tempodoro.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody LoginRequest request) {
        return authService.login(request.getUsername(), request.getPassword())
                .<ResponseEntity<Object>>map(token -> ResponseEntity.ok(new LoginResponse(token)))
                .orElseGet(() -> ResponseEntity.badRequest().body("Invalid credentials"));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        return authService.register(request.getUsername(), request.getPassword())
                .map(user -> ResponseEntity.ok("User registered successfully"))
                .orElseGet(() -> ResponseEntity.badRequest().body("Username already exists"));
    }
}