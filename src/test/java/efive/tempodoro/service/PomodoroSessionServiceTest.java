package efive.tempodoro.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import efive.tempodoro.dto.PomodoroSessionRequest;
import efive.tempodoro.dto.PomodoroSessionResponse;
import efive.tempodoro.entity.PomodoroSession;
import efive.tempodoro.entity.SessionStatus;
import efive.tempodoro.entity.User;
import efive.tempodoro.repository.PomodoroSessionRepository;
import efive.tempodoro.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class PomodoroSessionServiceTest {

    @Mock
    private PomodoroSessionRepository pomodoroSessionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PomodoroSessionService pomodoroSessionService;

    private User user = User.builder()
            .id(1L)
            .username("TestUser")
            .password("TestPassword")
            .build();

    private PomodoroSessionRequest request = PomodoroSessionRequest.builder()
            .userId(1L)
            .build();

    private Long sessionId = 10L;

    @Test
    void startSession_shouldReturnPomodoroSessionResponseSuccess() {
        when(pomodoroSessionRepository.findByUserIdAndStatus(user.getId(), SessionStatus.ACTIVE))
                .thenReturn(Optional.empty());

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));

        PomodoroSession savedSession = PomodoroSession.builder()
                .id(sessionId)
                .user(user)
                .startedAt(LocalDateTime.now())
                .completed(false)
                .build();

        when(pomodoroSessionRepository.save(any(PomodoroSession.class)))
                .thenReturn(savedSession);

        PomodoroSessionResponse response = pomodoroSessionService.startSession(request);
        assertThat(response)
                .isNotNull()
                .extracting(
                        PomodoroSessionResponse::getId,
                        PomodoroSessionResponse::getUserId,
                        PomodoroSessionResponse::getSessionDuration,
                        PomodoroSessionResponse::getBreakDuration,
                        PomodoroSessionResponse::getStatus,
                        PomodoroSessionResponse::getCompleted)
                .containsExactly(
                        10L,
                        1L,
                        25,
                        5,
                        SessionStatus.ACTIVE,
                        false);

        verify(pomodoroSessionRepository).findByUserIdAndStatus(1L, SessionStatus.ACTIVE);
        verify(userRepository).findById(1L);
        verify(pomodoroSessionRepository).save(any(PomodoroSession.class));
    }

    @Test
    void startSession_shouldThrowUserHasAlreadySession() {
        when(pomodoroSessionRepository.findByUserIdAndStatus(user.getId(), SessionStatus.ACTIVE))
                .thenThrow(IllegalStateException.class);

        assertThrows(IllegalStateException.class,
                () -> pomodoroSessionService.startSession(request));
    }

    @Test
    void startSession_shouldThrowUserNotFound() {
        when(pomodoroSessionRepository.findByUserIdAndStatus(user.getId(), SessionStatus.ACTIVE))
                .thenReturn(Optional.empty());
        when(userRepository.findById(user.getId()))
                .thenThrow(IllegalStateException.class);

        assertThrows(IllegalStateException.class,
                () -> pomodoroSessionService.startSession(request));
    }

}
