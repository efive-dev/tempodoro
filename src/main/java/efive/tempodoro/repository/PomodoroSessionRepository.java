package efive.tempodoro.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import efive.tempodoro.entity.PomodoroSession;

public interface PomodoroSessionRepository extends JpaRepository<PomodoroSession, Long> {

}
