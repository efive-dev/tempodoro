package efive.tempodoro.dto;

import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PomodoroSessionRequest {

    @Positive
    @Builder.Default
    private Integer sessionDuration = 25;

    @Positive
    @Builder.Default
    private Integer breakDuration = 5;
}
