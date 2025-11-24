package me.alsesn.alsoscore.model.dto;

import lombok.Data;
import me.alsesn.alsoscore.model.enums.TestStatus;

@Data
public class TestDto {
    private Long id;
    private String title;
    private String description;
    private TestStatus status;
    private int questionCount;
}