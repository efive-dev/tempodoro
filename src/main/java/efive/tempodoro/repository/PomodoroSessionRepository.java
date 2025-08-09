package efive.tempodoro.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import efive.tempodoro.entity.PomodoroSession;
import efive.tempodoro.entity.SessionStatus;

public interface PomodoroSessionRepository extends JpaRepository<PomodoroSession, Long> {
    Optional<PomodoroSession> findByUserIdAndStatus(Long userId, SessionStatus status);

    List<PomodoroSession> findByUserIdOrderByStartedAtDesc(Long userId);

    List<PomodoroSession> findByUserIdAndStartedAtBetweenOrderByStartedAtDesc(
            Long userId, LocalDateTime from, LocalDateTime to);
}
