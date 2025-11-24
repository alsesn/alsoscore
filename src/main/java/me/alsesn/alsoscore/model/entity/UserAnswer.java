package me.alsesn.alsoscore.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class UserAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private TestSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    private String submittedAnswer;

    private Boolean isCorrect;
    private Long timeToSolveMillis;
    private LocalDateTime questionStartTime;
    private LocalDateTime answerSubmitTime;

    @Column(columnDefinition = "TEXT")
    private String solvingLogicData;
}