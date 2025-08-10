package efive.tempodoro.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
            .sessionDuration(25)
            .breakDuration(5)
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
                .sessionDuration(request.getSessionDuration())
                .breakDuration(request.getBreakDuration())
                .status(SessionStatus.ACTIVE)
                .startedAt(LocalDateTime.now())
                .completed(false)
                .build();

        when(pomodoroSessionRepository.save(any(PomodoroSession.class)))
                .thenReturn(savedSession);

        PomodoroSessionResponse response = pomodoroSessionService.startSession(user.getId(), request);
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
                .thenReturn(Optional.of(
                        PomodoroSession.builder().id(1L).build()));

        assertThrows(IllegalStateException.class,
                () -> pomodoroSessionService.startSession(user.getId(), request));
    }

    @Test
    void startSession_shouldThrowUserNotFound() {
        when(pomodoroSessionRepository.findByUserIdAndStatus(user.getId(), SessionStatus.ACTIVE))
                .thenReturn(Optional.empty());

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> pomodoroSessionService.startSession(user.getId(), request));
    }

    @Test
    void stopSession_shouldBeFineSuccess() {
        PomodoroSession activeSession = PomodoroSession.builder()
                .id(sessionId)
                .user(user)
                .sessionDuration(25)
                .breakDuration(5)
                .status(SessionStatus.ACTIVE)
                .startedAt(LocalDateTime.now())
                .completed(false)
                .build();

        when(pomodoroSessionRepository.findByUserIdAndStatus(user.getId(), SessionStatus.ACTIVE))
                .thenReturn(Optional.of(activeSession));

        when(pomodoroSessionRepository.save(any(PomodoroSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PomodoroSessionResponse response = pomodoroSessionService.stopSession(user.getId());

        assertThat(response).isNotNull()
                .extracting(
                        PomodoroSessionResponse::getId,
                        PomodoroSessionResponse::getUserId,
                        PomodoroSessionResponse::getStatus,
                        PomodoroSessionResponse::getCompleted)
                .containsExactly(
                        sessionId,
                        user.getId(),
                        SessionStatus.STOPPED,
                        false);

        verify(pomodoroSessionRepository).findByUserIdAndStatus(user.getId(), SessionStatus.ACTIVE);
        verify(pomodoroSessionRepository).save(any(PomodoroSession.class));
    }

    @Test
    void stopSession_shouldThrowWhenNoActiveSession() {
        when(pomodoroSessionRepository.findByUserIdAndStatus(user.getId(), SessionStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> pomodoroSessionService.stopSession(user.getId()));

        verify(pomodoroSessionRepository).findByUserIdAndStatus(user.getId(), SessionStatus.ACTIVE);
    }

    @Test
    void completeSession_shouldBeFineSuccess() {
        PomodoroSession activeSession = PomodoroSession.builder()
                .id(sessionId)
                .user(user)
                .sessionDuration(25)
                .breakDuration(5)
                .status(SessionStatus.ACTIVE)
                .startedAt(LocalDateTime.now())
                .completed(false)
                .build();

        when(pomodoroSessionRepository.findByUserIdAndStatus(user.getId(), SessionStatus.ACTIVE))
                .thenReturn(Optional.of(activeSession));

        when(pomodoroSessionRepository.save(any(PomodoroSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PomodoroSessionResponse response = pomodoroSessionService.completeSession(user.getId());

        assertThat(response).isNotNull()
                .extracting(
                        PomodoroSessionResponse::getId,
                        PomodoroSessionResponse::getUserId,
                        PomodoroSessionResponse::getStatus,
                        PomodoroSessionResponse::getCompleted)
                .containsExactly(
                        sessionId,
                        user.getId(),
                        SessionStatus.COMPLETED,
                        true);

        verify(pomodoroSessionRepository).findByUserIdAndStatus(user.getId(), SessionStatus.ACTIVE);
        verify(pomodoroSessionRepository).save(any(PomodoroSession.class));
    }

    @Test
    void completeSession_shouldThrowWhenNoActiveSession() {
        when(pomodoroSessionRepository.findByUserIdAndStatus(user.getId(), SessionStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> pomodoroSessionService.completeSession(user.getId()));

        verify(pomodoroSessionRepository).findByUserIdAndStatus(user.getId(), SessionStatus.ACTIVE);
    }

    @Test
    void getSessionHistory_shouldReturnSessionHistoryNoParametersSuccess() {
        List<PomodoroSession> pomodoroSessions = new ArrayList<>();
        pomodoroSessions.add(PomodoroSession.builder()
                .id(sessionId)
                .user(user)
                .sessionDuration(25)
                .breakDuration(5)
                .status(SessionStatus.ACTIVE)
                .startedAt(LocalDateTime.now())
                .completed(false)
                .build());
        pomodoroSessions.add(PomodoroSession.builder()
                .id(sessionId + 1)
                .user(user)
                .sessionDuration(300)
                .breakDuration(100)
                .status(SessionStatus.STOPPED)
                .startedAt(LocalDateTime.now())
                .completed(false)
                .build());

        when(pomodoroSessionRepository.findByUserIdOrderByStartedAtDesc(user.getId())).thenReturn(pomodoroSessions);

        List<PomodoroSessionResponse> response = pomodoroSessionService.getSessionHistory(user.getId(), null, null);

        assertThat(response).hasSize(2);
        assertThat(response.get(0).getId()).isEqualTo(10L);
        assertThat(response.get(1).getId()).isEqualTo(11L);

        verify(pomodoroSessionRepository, times(1))
                .findByUserIdOrderByStartedAtDesc(user.getId());
    }

    @Test
    void getSessionHistory_NegativePath_ShouldReturnEmptyList() {
        when(pomodoroSessionRepository.findByUserIdOrderByStartedAtDesc(user.getId()))
                .thenReturn(Collections.emptyList());

        List<PomodoroSessionResponse> responses = pomodoroSessionService.getSessionHistory(user.getId(), null, null);

        assertThat(responses).isEmpty();

        verify(pomodoroSessionRepository, times(1))
                .findByUserIdOrderByStartedAtDesc(user.getId());
    }

    @Test
    void deleteSession_shouldDeleteSuccessfully() {
        PomodoroSession session = PomodoroSession.builder()
                .id(sessionId)
                .user(user)
                .build();

        when(pomodoroSessionRepository.findById(sessionId))
                .thenReturn(Optional.of(session));

        pomodoroSessionService.deleteSession(user.getId(), sessionId);

        verify(pomodoroSessionRepository).findById(sessionId);
        verify(pomodoroSessionRepository).delete(session);
    }

    @Test
    void deleteSession_shouldThrowWhenSessionNotFound() {
        when(pomodoroSessionRepository.findById(sessionId))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> pomodoroSessionService.deleteSession(user.getId(), sessionId));

        verify(pomodoroSessionRepository).findById(sessionId);
    }

    @Test
    void deleteSession_shouldThrowWhenUserDoesNotOwnSession() {
        User anotherUser = User.builder().id(999L).username("OtherUser").build();
        PomodoroSession session = PomodoroSession.builder()
                .id(sessionId)
                .user(anotherUser)
                .build();

        when(pomodoroSessionRepository.findById(sessionId))
                .thenReturn(Optional.of(session));

        assertThrows(SecurityException.class,
                () -> pomodoroSessionService.deleteSession(user.getId(), sessionId));

        verify(pomodoroSessionRepository).findById(sessionId);
    }

}
