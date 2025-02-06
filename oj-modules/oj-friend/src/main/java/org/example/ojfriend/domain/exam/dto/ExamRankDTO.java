package org.example.ojfriend.domain.exam.dto;

import lombok.Getter;
import lombok.Setter;
import org.example.common.core.domain.PageQueryDTO;

@Getter
@Setter
public class ExamRankDTO extends PageQueryDTO {

    private Long examId;
}
