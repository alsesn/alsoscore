package me.alsesn.alsoscore.repository;

import me.alsesn.alsoscore.model.entity.Test;
import me.alsesn.alsoscore.model.enums.TestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestRepository extends JpaRepository<Test, Long> {
    List<Test> findByStatus(TestStatus status);
}