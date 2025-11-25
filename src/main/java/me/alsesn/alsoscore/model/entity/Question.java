package me.alsesn.alsoscore.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import me.alsesn.alsoscore.model.enums.QuestionType;

@Entity
@Getter
@Setter
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    @JsonIgnore
    private Test test;

    private String text;

    @Enumerated(EnumType.STRING)
    private QuestionType type;

    private String  answerOptions;

    private String correctAnswer;

    private int scorePoints;
}