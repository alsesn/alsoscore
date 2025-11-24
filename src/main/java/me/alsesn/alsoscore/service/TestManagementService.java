package me.alsesn.alsoscore.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.alsesn.alsoscore.model.entity.Question;
import me.alsesn.alsoscore.model.entity.Test;
import me.alsesn.alsoscore.model.enums.TestStatus;
import me.alsesn.alsoscore.repository.QuestionRepository;
import me.alsesn.alsoscore.repository.TestRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TestManagementService {
    private final TestRepository testRepository;
    private final QuestionRepository questionRepository;

    @Transactional
    public Test saveOrUpdateTest(Test t) {
        return testRepository.save(t);
    }

    @Transactional
    public Question addQuestionToTest(Long tId, Question q) {
        Test test = testRepository.findById(tId)
                .orElseThrow(() -> new RuntimeException("Test not found"));
        q.setTest(test);

        return questionRepository.save(q);
    }

    @Transactional
    public Test publishTest(Long tId) {
        Test t = testRepository.findById(tId)
                .orElseThrow(() -> new RuntimeException("Test not found"));
        if (t.getQuestions() == null || t.getQuestions().isEmpty()) {
            throw new IllegalStateException("Cannot publish test without question");
        }

        t.setStatus(TestStatus.PUBLISHED);
        return testRepository.save(t);
    }

    public Optional<Test> getTestById(Long tId) {
        return testRepository.findById(tId);
    }
}