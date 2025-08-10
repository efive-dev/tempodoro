package efive.tempodoro.controller;

import efive.tempodoro.dto.PomodoroSessionRequest;
import efive.tempodoro.dto.PomodoroSessionResponse;
import efive.tempodoro.repository.UserRepository;
import efive.tempodoro.service.PomodoroSessionService;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

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

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = (String) authentication.getPrincipal();
        Long userId = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();

        PomodoroSessionResponse sessionResponse = pomodoroSessionService.stopSession(userId);
        return ResponseEntity.ok(sessionResponse);
    }

    @PatchMapping("/complete")
    public ResponseEntity<PomodoroSessionResponse> completeSession(
            Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = (String) authentication.getPrincipal();
        Long userId = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();

        PomodoroSessionResponse sessionResponse = pomodoroSessionService.completeSession(userId);
        return ResponseEntity.ok(sessionResponse);
    }

    @GetMapping("/history")
    public ResponseEntity<List<PomodoroSessionResponse>> getSessionHistory(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = (String) authentication.getPrincipal();
        Long userId = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();

        LocalDateTime fromDate = (from != null) ? LocalDateTime.parse(from) : null;
        LocalDateTime toDate = (to != null) ? LocalDateTime.parse(to) : null;

        List<PomodoroSessionResponse> sessions = pomodoroSessionService.getSessionHistory(userId, fromDate, toDate);
        return ResponseEntity.ok(sessions);
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> deleteSession(
            @PathVariable Long sessionId,
            Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = (String) authentication.getPrincipal();
        Long userId = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();

        try {
            pomodoroSessionService.deleteSession(userId, sessionId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

}