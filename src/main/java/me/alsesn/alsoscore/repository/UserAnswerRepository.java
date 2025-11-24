package me.alsesn.alsoscore.repository;

import me.alsesn.alsoscore.model.entity.UserAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {
    List<UserAnswer> findBySession_SessionId(String sessionId);
}