package org.example.ojsystem.domain.exam.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.example.common.core.domain.PageQueryDTO;


@Getter
@Setter
public class ExamQueryDTO extends PageQueryDTO {

    private String title;

    private String startTime;

    private String endTime;
}
