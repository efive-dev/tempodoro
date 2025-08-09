package efive.tempodoro.controller;

import efive.tempodoro.dto.PomodoroSessionRequest;
import efive.tempodoro.dto.PomodoroSessionResponse;
import efive.tempodoro.repository.UserRepository;
import efive.tempodoro.service.PomodoroSessionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/pomodoro")
public class PomodoroSessionController {

    @Autowired
    private PomodoroSessionService pomodoroSessionService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/start")
    public ResponseEntity<PomodoroSessionResponse> startSession(
            @Valid @RequestBody PomodoroSessionRequest request,
            Authentication authentication) {
        // Principal in JwtAuthenticationFilter is just the username (String)
        String username = (String) authentication.getPrincipal();

        Long userId = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();

        PomodoroSessionResponse sessionResponse = pomodoroSessionService.startSession(userId, request);

        return ResponseEntity.ok(sessionResponse);
    }

    @PatchMapping("/stop")
    public ResponseEntity<PomodoroSessionResponse> stopSession(
            Authentication authentication) {
        String username = (String) authentication.getPrincipal();

        Long userId = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();

        PomodoroSessionResponse sessionResponse = pomodoroSessionService.stopSession(userId);

        return ResponseEntity.ok(sessionResponse);
    }

}
