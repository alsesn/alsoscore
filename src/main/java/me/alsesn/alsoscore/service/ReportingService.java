package me.alsesn.alsoscore.service;

import lombok.RequiredArgsConstructor;
import me.alsesn.alsoscore.model.entity.TestSession;
import me.alsesn.alsoscore.model.entity.UserAnswer;
import me.alsesn.alsoscore.model.enums.SessionStatus;
import me.alsesn.alsoscore.repository.TestSessionRepository;
import me.alsesn.alsoscore.repository.UserAnswerRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportingService {
    private final TestSessionRepository sessionRepository;
    private final UserAnswerRepository userAnswerRepository;

    @Async
    public void processSessionResultAsync(String sessionId) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        TestSession session = sessionRepository.findById(sessionId)
                .orElse(null);

        if (session == null || session.getFinishTime() == null) {
            return;
        }

        List<UserAnswer> answers = userAnswerRepository.findBySession_SessionId(sessionId);

        long totalTimeMillis = java.time.Duration.between(session.getStartTime(), session.getFinishTime()).toMillis();

        double totalScore = 0;
        long correctCount = 0;
        long totalAnsweredQuestion = answers.size();
        long totalTimeSolved = 0;

        for (UserAnswer answer : answers) {
            if (Boolean.TRUE.equals(answer.getIsCorrect())) {
                totalScore += answer.getQuestion().getScorePoints();
                correctCount++;
            }
            totalTimeSolved += answer.getTimeToSolveMillis();
        }

        double correctAnswersPercentage = (totalAnsweredQuestion > 0)
                ? ((double) correctCount / totalAnsweredQuestion) * 100
                : 0.0;
        double averageTimePerQuestionMillis = (totalAnsweredQuestion > 0)
                ? (double) totalTimeSolved / totalAnsweredQuestion
                : 0.0;
        session.setTotalTimeMillis(totalTimeMillis);
        session.setTotalScore(totalScore);
        session.setCorrectAnswersPercentage(correctAnswersPercentage);
        session.setAverageTimePerQuestionMillis(averageTimePerQuestionMillis);
        session.setStatus(SessionStatus.REPORT_READY);

        sessionRepository.save(session);
    }

    public TestSession getDetailReport(String sessionId) {
        TestSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        if (session.getStatus() != SessionStatus.REPORT_READY) {
            throw new RuntimeException("Report is still processing or session is incomplete");
        }

        return session;
    }

}