package org.example.ojfriend.domain.question.dto;

import lombok.Getter;
import lombok.Setter;
import org.example.common.core.domain.PageQueryDTO;

@Getter
@Setter
public class QuestionQueryDTO extends PageQueryDTO {

    private String keyword;

    private Integer difficulty;
}
