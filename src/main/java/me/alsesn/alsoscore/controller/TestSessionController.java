package me.alsesn.alsoscore.controller;

import lombok.RequiredArgsConstructor;
import me.alsesn.alsoscore.model.entity.Question;
import me.alsesn.alsoscore.model.entity.TestSession;
import me.alsesn.alsoscore.model.entity.UserAnswer;
import me.alsesn.alsoscore.service.ReportingService;
import me.alsesn.alsoscore.service.TestSessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TestSessionController {
    private final TestSessionService sessionService;
    private final ReportingService reportingService;

    @PostMapping("/sessions/start/{testId}")
    public ResponseEntity<Map<String, String>> startSession(@PathVariable Long testId,
                                                            @AuthenticationPrincipal UserDetails userDetails) {
        Long mockUserId = (long) userDetails.getUsername().hashCode();
        TestSession session = sessionService.startNewSession(testId, mockUserId);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of("sessionId", session.getSessionId(),
                        "message", "Sessions started successfully"));
    }

    @GetMapping("/sessions/{sessionId}/next-question")
    public ResponseEntity<Question> getNextQuestion(@PathVariable String sessionId) {
        Question question = sessionService.getNextQuestion(sessionId);

        if (question == null) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        Question safeQuestion = new Question();
        safeQuestion.setId(question.getId());
        safeQuestion.setText(question.getText());
        safeQuestion.setType(question.getType());
        safeQuestion.setAnswerOptions(question.getAnswerOptions());
        safeQuestion.setScorePoints(question.getScorePoints());

        return ResponseEntity.ok(safeQuestion);
    }

    @PostMapping("/sessions/{sessionId}/answer")
    public ResponseEntity<UserAnswer> submitAnswer(@PathVariable String sessionId,
                                                   @RequestBody Map<String, String> answerData) {
        Long questionId = Long.valueOf(answerData.get("questionId"));
        String submittedAnswer = answerData.get("submittedAnswer");
        String solvingLogic = answerData.getOrDefault("solvingLogic", null);

        UserAnswer userAnswer = sessionService.submitAnswer(
                sessionId, questionId, submittedAnswer, solvingLogic
        );

        return ResponseEntity.ok(userAnswer);
    }

    @PostMapping("/sessions/{sessionId}/finish")
    public ResponseEntity<Map <String, String >> finishSession(@PathVariable String sessionId) {
        sessionService.finishSession(sessionId);

        return ResponseEntity.ok(
                Map.of("message", "Sessions finished. Results are being processed asynchronously."));
    }

    @GetMapping("/reports/{sessionId}")
    public ResponseEntity<TestSession> getReport(@PathVariable String sessionId) {
        TestSession report = reportingService.getDetailReport(sessionId);

        return ResponseEntity.ok(report);
    }
}