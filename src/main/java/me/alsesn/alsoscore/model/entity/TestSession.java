package me.alsesn.alsoscore.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import me.alsesn.alsoscore.model.enums.SessionStatus;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class TestSession {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    private Test test;

    // TODO
    private Long userId;

    private LocalDateTime startTime;
    private LocalDateTime finishTime;

    @Enumerated(EnumType.STRING)
    private SessionStatus status = SessionStatus.IN_PROGRESS;

    private Double totalScore;
    private Long totalTimeMillis;
    private Double correctAnswersPercentage;
    private Double averageTimePerQuestionMillis;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserAnswer> userAnswer;
}