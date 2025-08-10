package efive.tempodoro.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import efive.tempodoro.dto.PomodoroSessionRequest;
import efive.tempodoro.dto.PomodoroSessionResponse;
import efive.tempodoro.entity.SessionStatus;
import efive.tempodoro.entity.User;
import efive.tempodoro.repository.UserRepository;
import efive.tempodoro.service.PomodoroSessionService;

@ExtendWith(MockitoExtension.class)
public class PomodoroControllerTest {

    @Mock
    private PomodoroSessionService pomodoroSessionService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PomodoroSessionController pomodoroSessionController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();
    private String username = "testUsername";
    private String password = "testPassword";
    private Long userId = 40L;
    private User user = User.builder()
            .id(userId)
            .username(username)
            .password(password)
            .build();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(pomodoroSessionController)
                .build();
    }

    @Test
    void startSession_shouldStartSessionSuccess() throws Exception {
        PomodoroSessionRequest request = PomodoroSessionRequest.builder()
                .sessionDuration(25)
                .breakDuration(15)
                .build();

        PomodoroSessionResponse response = PomodoroSessionResponse.builder()
                .id(1L)
                .userId(user.getId())
                .status(SessionStatus.ACTIVE)
                .startedAt(LocalDateTime.now())
                .completed(false)
                .build();

        when(authentication.getPrincipal()).thenReturn(user.getUsername());
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(pomodoroSessionService.startSession(eq(user.getId()), any(PomodoroSessionRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/pomodoro/start")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.userId").value(response.getUserId()))
                .andExpect(jsonPath("$.sessionDuration").value(response.getSessionDuration()))
                .andExpect(jsonPath("$.breakDuration").value(response.getBreakDuration()))
                .andExpect(jsonPath("$.status").value(response.getStatus().toString()))
                .andExpect(jsonPath("$.completed").value(response.getCompleted()));
    }

    @Test
    void startSession_shouldReturn401MissingAuthentication() throws Exception {
        PomodoroSessionRequest request = PomodoroSessionRequest.builder().build();

        mockMvc.perform(post("/api/pomodoro/start")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void stopSession_shouldStopSessionSuccess() throws Exception {
        PomodoroSessionResponse response = PomodoroSessionResponse.builder()
                .id(1L)
                .userId(user.getId())
                .status(SessionStatus.STOPPED)
                .startedAt(LocalDateTime.now())
                .completed(false)
                .build();

        when(authentication.getPrincipal()).thenReturn(user.getUsername());
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(pomodoroSessionService.stopSession(eq(user.getId()))).thenReturn(response);

        mockMvc.perform(patch("/api/pomodoro/stop")
                .contentType(MediaType.APPLICATION_JSON)
                .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.userId").value(response.getUserId()))
                .andExpect(jsonPath("$.sessionDuration").value(response.getSessionDuration()))
                .andExpect(jsonPath("$.breakDuration").value(response.getBreakDuration()))
                .andExpect(jsonPath("$.status").value(response.getStatus().toString()))
                .andExpect(jsonPath("$.completed").value(response.getCompleted()));
    }

    @Test
    void stopSession_shouldReturn401MissingAuthentication() throws Exception {
        mockMvc.perform(patch("/api/pomodoro/stop")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void completeSession_shouldCompleteSessionSuccess() throws Exception {
        PomodoroSessionResponse response = PomodoroSessionResponse.builder()
                .id(1L)
                .userId(user.getId())
                .status(SessionStatus.COMPLETED)
                .startedAt(LocalDateTime.now().minusMinutes(25))
                .completed(true)
                .build();

        when(authentication.getPrincipal()).thenReturn(user.getUsername());
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(pomodoroSessionService.completeSession(eq(user.getId()))).thenReturn(response);

        mockMvc.perform(patch("/api/pomodoro/complete")
                .contentType(MediaType.APPLICATION_JSON)
                .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.userId").value(response.getUserId()))
                .andExpect(jsonPath("$.sessionDuration").value(response.getSessionDuration()))
                .andExpect(jsonPath("$.breakDuration").value(response.getBreakDuration()))
                .andExpect(jsonPath("$.status").value(response.getStatus().toString()))
                .andExpect(jsonPath("$.completed").value(response.getCompleted()));
    }

    @Test
    void completeSession_shouldReturn401MissingAuthentication() throws Exception {
        mockMvc.perform(patch("/api/pomodoro/complete")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getSessionHistory_shouldReturnHistorySuccess() throws Exception {
        PomodoroSessionResponse session1 = PomodoroSessionResponse.builder()
                .id(1L)
                .userId(user.getId())
                .status(SessionStatus.COMPLETED)
                .startedAt(LocalDateTime.now().minusDays(1))
                .completed(true)
                .build();

        PomodoroSessionResponse session2 = PomodoroSessionResponse.builder()
                .id(2L)
                .userId(user.getId())
                .status(SessionStatus.STOPPED)
                .startedAt(LocalDateTime.now().minusHours(5))
                .completed(false)
                .build();

        when(authentication.getPrincipal()).thenReturn(user.getUsername());
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(pomodoroSessionService.getSessionHistory(eq(user.getId()), any(), any()))
                .thenReturn(List.of(session1, session2));

        mockMvc.perform(get("/api/pomodoro/history")
                .param("from", LocalDateTime.now().minusDays(2).toString())
                .param("to", LocalDateTime.now().toString())
                .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(session1.getId()))
                .andExpect(jsonPath("$[0].userId").value(session1.getUserId()))
                .andExpect(jsonPath("$[0].status").value(session1.getStatus().toString()))
                .andExpect(jsonPath("$[0].completed").value(session1.getCompleted()))
                .andExpect(jsonPath("$[1].id").value(session2.getId()))
                .andExpect(jsonPath("$[1].userId").value(session2.getUserId()))
                .andExpect(jsonPath("$[1].status").value(session2.getStatus().toString()))
                .andExpect(jsonPath("$[1].completed").value(session2.getCompleted()));
    }

    @Test
    void getSessionHistory_shouldReturn401MissingAuthentication() throws Exception {
        mockMvc.perform(get("/api/pomodoro/history"))
                .andExpect(status().isUnauthorized());
    }
}
