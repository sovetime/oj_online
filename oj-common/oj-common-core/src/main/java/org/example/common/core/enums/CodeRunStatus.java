package org.example.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CodeRunStatus {

    PENDING(0,"状态待定"),
    RUNNING(1, "运行中"),
    SUCCEED(2, "运行成功"),
    FAILED(3, "运行失败"),

    NOT_ALL_PASSED(4, "未通过所有用例"),
    UNKNOWN_FAILED(5, "未知异常，请您稍后重试"),

    COMPILE_FAILED(6, "编译失败"),

    OUT_OF_MEMORY(7, "运行结果正确，但是超出空间限制"),
    OUT_OF_TIME(8, "运行结果正确，但是超出时间限制");
    private Integer value;

    private String msg;

}
