package org.example.ojsystem.domain.exam.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;

//竞赛中题目添加操作
@Getter
@Setter
public class ExamQuestAddDTO {

    private Long examId;

    //保持元素插入的顺序，并且不允许重复元素
    private LinkedHashSet<Long> questionIdSet;
}
