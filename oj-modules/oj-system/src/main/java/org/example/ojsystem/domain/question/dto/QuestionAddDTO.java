package org.example.ojsystem.domain.question.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * Date: 2025-01-14
 * Time: 21:54
 */
@Getter
@Setter
public class QuestionAddDTO {

    private String title;

    private Integer difficulty;

    private Long timeLimit;

    private Long spaceLimit;

    private String content;

    private String questionCase;

    private String defaultCode;

    private String mainFuc;
}
