package me.alsesn.alsoscore.repository;

import me.alsesn.alsoscore.model.entity.TestSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestSessionRepository extends JpaRepository<TestSession, String> {
}