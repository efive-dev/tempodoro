package efive.tempodoro.service;

import java.time.LocalDateTime;
import java.util.Optional;

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

    public PomodoroSessionResponse startSession(PomodoroSessionRequest request) {
        pomodoroSessionRepository
                .findByUserIdAndStatus(request.getUserId(), SessionStatus.ACTIVE)
                .ifPresent(session -> {
                    throw new IllegalStateException("User already has an active session");
                });

        User user = userRepository.findById(request.getUserId())
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
