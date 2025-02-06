package org.example.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum QuestionResType {

    ERROR(0), //未通过
    PASS(1), //通过
    UN_SUBMIT(2),  //未提交
    IN_JUDGE(3); //  系统判题中

    private Integer value;
}
