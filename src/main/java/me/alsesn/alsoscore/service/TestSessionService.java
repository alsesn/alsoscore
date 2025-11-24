package me.alsesn.alsoscore.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.alsesn.alsoscore.model.entity.Question;
import me.alsesn.alsoscore.model.entity.Test;
import me.alsesn.alsoscore.model.entity.TestSession;
import me.alsesn.alsoscore.model.entity.UserAnswer;
import me.alsesn.alsoscore.model.enums.SessionStatus;
import me.alsesn.alsoscore.model.enums.TestStatus;
import me.alsesn.alsoscore.repository.QuestionRepository;
import me.alsesn.alsoscore.repository.TestRepository;
import me.alsesn.alsoscore.repository.TestSessionRepository;
import me.alsesn.alsoscore.repository.UserAnswerRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TestSessionService {

    private final TestRepository testRepository;
    private final TestSessionRepository sessionRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final QuestionRepository questionRepository;
    private final ReportingService reportingService;

    @Transactional
    public TestSession startNewSession(Long tId, Long uId) {
        Test t = testRepository.findById(tId)
                .orElseThrow(() -> new RuntimeException("Test not found"));
        if (t.getStatus() != TestStatus.PUBLISHED) {
            throw new IllegalStateException("Test is not published and cannot be started");
        }

        TestSession s = new TestSession();
        s.setTest(t);
        s.setUserId(uId);
        s.setStartTime(LocalDateTime.now());
        s.setStatus(SessionStatus.IN_PROGRESS);

        return sessionRepository.save(s);
    }

    public Question getNextQuestion(String sId) {
        TestSession s = sessionRepository.findById(sId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        List<Question> allQuestions = s.getTest().getQuestions();
        List<UserAnswer> answered = userAnswerRepository.findBySession_SessionId(sId);

        for (Question q : allQuestions) {
            boolean alreadyAnswered = answered.stream()
                    .anyMatch(a -> a.getQuestion().getId().equals(q.getId()));
            if (!alreadyAnswered) {
                return q;
            }
        }
        return null;
    }

    public UserAnswer submitAnswer(String sId, Long qId, String submittedAnswer, String solvingLogic) {
        TestSession s = sessionRepository.findById(sId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        if (s.getStatus() != SessionStatus.IN_PROGRESS) {
            throw new RuntimeException("Session is not active");
        }

        Question q = questionRepository.findById(qId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        boolean isCorrect = submittedAnswer.equalsIgnoreCase(q.getCorrectAnswer());

        LocalDateTime answerSubmitTime = LocalDateTime.now();
        LocalDateTime questionStartTime = answerSubmitTime.minusSeconds(15);

        long timeToSolveMillis = Duration.between(questionStartTime, answerSubmitTime).toMillis();

        UserAnswer uAnswer = new UserAnswer();
        uAnswer.setSession(s);
        uAnswer.setQuestion(q);
        uAnswer.setSubmittedAnswer(submittedAnswer);
        uAnswer.setIsCorrect(isCorrect);
        uAnswer.setQuestionStartTime(questionStartTime);
        uAnswer.setAnswerSubmitTime(answerSubmitTime);
        uAnswer.setTimeToSolveMillis(timeToSolveMillis);
        uAnswer.setSolvingLogicData(solvingLogic);

        return userAnswerRepository.save(uAnswer);
    }

    @Transactional
    public TestSession finishSession(String sId) {
        TestSession s = sessionRepository.findById(sId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        if (s.getStatus() != SessionStatus.FINISHED || s.getStatus() == SessionStatus.REPORT_READY) {
            throw new RuntimeException("Session is already finished.");
        }

        s.setFinishTime(LocalDateTime.now());
        s.setStatus(SessionStatus.FINISHED);
        TestSession savedSession = sessionRepository.save(s);

        reportingService.processSessionResultsAsync(savedSession.getSessionId());
        return savedSession;
    }
}