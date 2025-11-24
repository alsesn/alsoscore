package me.alsesn.alsoscore.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import me.alsesn.alsoscore.model.enums.QuestionType;

@Data
public class QuestionCreateDto {
    @NotBlank(message = "Текст вопроса не можем быть пустым.")
    private String text;

    @NotNull(message = "Тима вопроса быть указан.")
    private QuestionType type;

    private String answerOptions;

    @NotBlank(message = "Правильный ответ не должен быть пустым")
    private String correctAnswer;

    @Min(value = 1, message = "Балл должен быть не менее 1.")
    private int scorePoints;
}