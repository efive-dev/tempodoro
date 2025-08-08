package efive.tempodoro.dto;

import java.time.LocalDateTime;

import efive.tempodoro.entity.SessionStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PomodoroSessionResponse {
    private Long id;
    private Long userId;
    private Integer sessionDuration;
    private Integer breakDuration;
    private SessionStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime stoppedAt;
    private Boolean completed;
    private LocalDateTime completedAt;
}
