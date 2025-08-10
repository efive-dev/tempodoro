package efive.tempodoro.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import efive.tempodoro.dto.PomodoroSessionRequest;
import efive.tempodoro.dto.PomodoroSessionResponse;
import efive.tempodoro.entity.PomodoroSession;
import efive.tempodoro.entity.SessionStatus;
import efive.tempodoro.entity.User;
import efive.tempodoro.repository.PomodoroSessionRepository;
import efive.tempodoro.repository.UserRepository;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class PomodoroSessionService {

    @Autowired
    private PomodoroSessionRepository pomodoroSessionRepository;

    @Autowired
    private UserRepository userRepository;

    public PomodoroSessionResponse startSession(Long userId, PomodoroSessionRequest request) {
        pomodoroSessionRepository
                .findByUserIdAndStatus(userId, SessionStatus.ACTIVE)
                .ifPresent(session -> {
                    throw new IllegalStateException("User already has an active session");
                });

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        PomodoroSession pomodoroSession = PomodoroSession.builder()
                .user(user)
                .sessionDuration(request.getSessionDuration())
                .breakDuration(request.getBreakDuration())
                .status(SessionStatus.ACTIVE)
                .startedAt(LocalDateTime.now())
                .completed(false)
                .build();

        return Optional.of(pomodoroSession)
                .map(pomodoroSessionRepository::save)
                .map(this::convertToResponse)
                .orElseThrow(() -> new RuntimeException("Failed to create session"));
    }

    public PomodoroSessionResponse stopSession(Long userId) {
        PomodoroSession pomodoroSession = pomodoroSessionRepository
                .findByUserIdAndStatus(userId, SessionStatus.ACTIVE)
                .orElseThrow(() -> new IllegalStateException("No active session found"));

        pomodoroSession.setStatus(SessionStatus.STOPPED);
        pomodoroSession.setStoppedAt(LocalDateTime.now());

        return Optional.of(pomodoroSession)
                .map(pomodoroSessionRepository::save)
                .map(this::convertToResponse)
                .orElseThrow(() -> new RuntimeException("Failed to stop the session"));
    }

    public PomodoroSessionResponse completeSession(Long userId) {
        PomodoroSession pomodoroSession = pomodoroSessionRepository
                .findByUserIdAndStatus(userId, SessionStatus.ACTIVE)
                .orElseThrow(() -> new IllegalStateException("No active session found"));

        LocalDateTime now = LocalDateTime.now();
        pomodoroSession.setStatus(SessionStatus.COMPLETED);
        pomodoroSession.setStoppedAt(now);
        pomodoroSession.setCompleted(true);
        pomodoroSession.setCompletedAt(now);

        return Optional.of(pomodoroSession)
                .map(pomodoroSessionRepository::save)
                .map(this::convertToResponse)
                .orElseThrow(() -> new RuntimeException("Failed to stop the session"));
    }

    public List<PomodoroSessionResponse> getSessionHistory(Long userId, LocalDateTime from, LocalDateTime to) {
        List<PomodoroSession> sessions;

        if (from != null && to != null) {
            sessions = pomodoroSessionRepository.findByUserIdAndStartedAtBetweenOrderByStartedAtDesc(userId, from, to);
        } else {
            sessions = pomodoroSessionRepository.findByUserIdOrderByStartedAtDesc(userId);
        }

        return sessions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public void deleteSession(Long userId, Long sessionId) {
        PomodoroSession pomodoroSession = pomodoroSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        if (!pomodoroSession.getUser().getId().equals(userId)) {
            throw new SecurityException("You do not have permission to delete this session");
        }

        pomodoroSessionRepository.delete(pomodoroSession);
    }

    private PomodoroSessionResponse convertToResponse(PomodoroSession pomodoroSession) {
        return PomodoroSessionResponse.builder()
                .id(pomodoroSession.getId())
                .userId(pomodoroSession.getUser().getId())
                .sessionDuration(pomodoroSession.getSessionDuration())
                .breakDuration(pomodoroSession.getBreakDuration())
                .status(pomodoroSession.getStatus())
                .startedAt(pomodoroSession.getStartedAt())
                .stoppedAt(pomodoroSession.getStoppedAt())
                .completed(pomodoroSession.getCompleted())
                .completedAt(pomodoroSession.getCompletedAt())
                .build();
    }
}
