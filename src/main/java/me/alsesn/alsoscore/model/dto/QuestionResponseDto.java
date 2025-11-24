package me.alsesn.alsoscore.model.dto;

import lombok.Data;
import me.alsesn.alsoscore.model.enums.QuestionType;

@Data
public class QuestionResponseDto {
    private Long id;
    private String text;
    private QuestionType type;
    private String answerOptions;
}