package me.alsesn.alsoscore.repository;

import me.alsesn.alsoscore.model.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {
}