package me.alsesn.alsoscore.mapper;

import me.alsesn.alsoscore.model.dto.TestDto;
import me.alsesn.alsoscore.model.entity.Test;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TestMapper {

    @Mapping(target = "questionCount", expression = "java(test.getQuestions().size())")
    TestDto toDto(Test test);

    Test toEntity(TestDto testDto);

    List<TestDto> toDtoList(List<Test> tests);
}