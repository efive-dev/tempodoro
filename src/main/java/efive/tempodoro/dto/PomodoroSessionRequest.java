package efive.tempodoro.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PomodoroSessionRequest {

    @NotNull
    private Long userId;

    @Positive
    private Integer sessionDuration = 25;

    @Positive
    private Integer breakDuration = 5;
}
