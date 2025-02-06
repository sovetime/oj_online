package org.example.ojsystem.domain.question.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.example.common.core.domain.PageQueryDTO;

import java.util.Set;


@Getter
@Setter
public class QuestionQueryDTO extends PageQueryDTO {

    private Integer difficulty;

    private String title;

    private String excludeIdStr;

    private Set<Long> excludeIdSet;
}
