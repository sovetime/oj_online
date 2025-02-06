package org.example.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExamListType {

    EXAM_UN_FINISH_LIST(0), //竞赛还在进行中的状态
    EXAM_HISTORY_LIST(1),   //历史竞赛
    USER_EXAM_LIST(2);      //用户参加的竞赛

    private final Integer value;
}