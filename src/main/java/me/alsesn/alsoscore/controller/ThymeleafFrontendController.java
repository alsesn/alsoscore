package me.alsesn.alsoscore.controller;

import lombok.RequiredArgsConstructor;
import me.alsesn.alsoscore.model.dto.QuestionCreateDto;
import me.alsesn.alsoscore.model.entity.Question;
import me.alsesn.alsoscore.model.entity.Test;
import me.alsesn.alsoscore.service.ReportingService;
import me.alsesn.alsoscore.service.TestManagementService;
import me.alsesn.alsoscore.service.TestSessionService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class ThymeleafFrontendController {

    private final TestManagementService testManagementService;
    private final TestSessionService testSessionService;
    private final ReportingService reportingService;

    // --- 1. Админ-Панель (Управление Тестами) ---

    @GetMapping("/admin/tests")
    public String adminPanel(Model model) {
        model.addAttribute("tests", testManagementService.getAllTest());
        model.addAttribute("newTest", new Test());
        return "admin-tests";
    }

    @PostMapping("/admin/tests/create")
    public String createTest(@ModelAttribute Test test) {
        testManagementService.saveOrUpdateTest(test);
        return "redirect:/admin/tests";
    }

    @GetMapping("/admin/tests/{testId}/edit")
    public String editTest(@PathVariable Long testId, Model model) {
        Test test = testManagementService.getTestById(testId)
                .orElseThrow(() -> new RuntimeException("Test not found")); // В реальном проекте - ResourceNotFoundException
        model.addAttribute("test", test);
        model.addAttribute("questionDto", new QuestionCreateDto());
        return "admin-edit-test";
    }

    @PostMapping("/admin/tests/{testId}/publish")
    public String publishTest(@PathVariable Long testId) {
        testManagementService.publishTest(testId);
        return "redirect:/admin/tests";
    }

    @GetMapping("/user/start")
    public String userStart(Model model) {
        model.addAttribute("publishedTests", testManagementService.getPublishedTests()); // Нужен новый метод getPublishedTests()
        return "user-start";
    }

    @PostMapping("/user/session/start/{testId}")
    public String startSession(@PathVariable Long testId,
                               @AuthenticationPrincipal UserDetails userDetails) {
        Long mockUserId = (long) userDetails.getUsername().hashCode();

        String sessionId = testSessionService.startNewSession(testId, mockUserId).getSessionId();

        return "redirect:/user/session/" + sessionId + "/question";
    }

    @GetMapping("/user/session/{sessionId}/question")
    public String getNextQuestion(@PathVariable String sessionId, Model model) {
        Question nextQuestion = testSessionService.getNextQuestion(sessionId);

        if (nextQuestion == null) {
            return "redirect:/user/session/" + sessionId + "/finish";
        }

        model.addAttribute("question", nextQuestion);
        model.addAttribute("sessionId", sessionId);
        model.addAttribute("questionStartTime", System.currentTimeMillis());

        return "user-question";
    }

    @PostMapping("/user/session/{sessionId}/answer/{questionId}")
    public String submitAnswer(@PathVariable String sessionId,
                               @PathVariable Long questionId,
                               @RequestParam String submittedAnswer,
                               @RequestParam Long questionStartTime) {
        testSessionService.submitAnswerWithTimeMetrics(
                sessionId,
                questionId,
                submittedAnswer,
                questionStartTime,
                null
        );

        return "redirect:/user/session/" + sessionId + "/question";
    }

    @GetMapping("/user/session/{sessionId}/finish")
    public String finishSession(@PathVariable String sessionId, RedirectAttributes redirectAttributes) {
        testSessionService.finishSession(sessionId);

        redirectAttributes.addFlashAttribute("message", "Тест завершен. Идет расчет результатов...");

        return "redirect:/user/report/" + sessionId;
    }

    @GetMapping("/user/report/{sessionId}")
    public String getReport(@PathVariable String sessionId, Model model) {
        model.addAttribute("report", reportingService.getDetailReport(sessionId));
        return "user-report";
    }
}