package me.alsesn.alsoscore.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.alsesn.alsoscore.mapper.TestMapper;
import me.alsesn.alsoscore.model.dto.QuestionCreateDto;
import me.alsesn.alsoscore.model.dto.TestDto;
import me.alsesn.alsoscore.model.entity.Question;
import me.alsesn.alsoscore.model.entity.Test;
import me.alsesn.alsoscore.service.TestManagementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tests")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'CREATOR')")
public class TestManagementController {
    private final TestManagementService testManagementService;
    private final TestMapper mapper;

    @PostMapping
    public ResponseEntity<TestDto> createTest(@Valid @RequestBody TestDto testDto) {
        Test testEntity = mapper.toEntity(testDto);

        Test createdTest = testManagementService.saveOrUpdateTest(testEntity);
        return new ResponseEntity<>(mapper.toDto(createdTest), HttpStatus.CREATED);
    }

    @GetMapping("/{testId}")
    public ResponseEntity<TestDto> getTest(@PathVariable Long testId) {
        Test test = testManagementService.getTestById(testId)
                .orElseThrow(() -> new RuntimeException("Test not found with ID: " + testId));
        return ResponseEntity.ok(mapper.toDto(test));
    }

    @PutMapping("/{testId}/questions")
    public ResponseEntity<Question> addQuestion(@PathVariable Long testId,
                                                @Valid @RequestBody QuestionCreateDto questionDto) {
        Question newQuestion = new Question();
        newQuestion.setText(questionDto.getText());
        newQuestion.setType(questionDto.getType());
        newQuestion.setCorrectAnswer(questionDto.getCorrectAnswer());
        newQuestion.setScorePoints(questionDto.getScorePoints());
        newQuestion.setAnswerOptions(questionDto.getAnswerOptions());

        Question createdQuestion = testManagementService.addQuestionToTest(testId, newQuestion);

        return ResponseEntity.ok(createdQuestion);
    }

    @PostMapping("/{testId}/publish")
    public ResponseEntity<Test> publishTest(@PathVariable Long testId) {
        Test publishTest = testManagementService.publishTest(testId);
        return ResponseEntity.ok(publishTest);
    }
}