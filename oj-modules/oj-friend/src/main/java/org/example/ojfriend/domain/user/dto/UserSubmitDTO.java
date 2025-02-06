package org.example.ojfriend.domain.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSubmitDTO {

    //根据examId查询出该场考试的时间限制和空间限制
    private Long examId;

    //根据题目id从es中查询出题目的测试用例和main函数，将代码进行拼接，再进行编译和运行
    private Long questionId;

    //编程语言类型，为了方便后续扩展其他语言功能
    private Integer programType;

    private String userCode;
}
