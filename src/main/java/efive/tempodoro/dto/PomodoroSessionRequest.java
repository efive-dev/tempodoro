package efive.tempodoro.dto;

import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@Getter
@Setter
@Builder
@JsonDeserialize(builder = PomodoroSessionRequest.PomodoroSessionRequestBuilder.class)
public class PomodoroSessionRequest {

    @Positive
    @Builder.Default
    private Integer sessionDuration = 25;

    @Positive
    @Builder.Default
    private Integer breakDuration = 5;

    @JsonPOJOBuilder(withPrefix = "")
    public static class PomodoroSessionRequestBuilder {
    }
}
