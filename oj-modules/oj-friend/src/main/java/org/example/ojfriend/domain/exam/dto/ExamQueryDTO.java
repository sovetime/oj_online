package org.example.ojfriend.domain.exam.dto;

import lombok.Getter;
import lombok.Setter;
import org.example.common.core.domain.PageQueryDTO;

@Getter
@Setter
public class ExamQueryDTO extends PageQueryDTO {

    private String title;

    private String startTime;

    private String endTime;

    private Integer type; //0-还在进行中  1-历史竞赛  2-用户参加的
}
