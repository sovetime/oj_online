package org.example.ojsystem.domain.question.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionEditDTO extends QuestionAddDTO{

    private Long questionId;
}
