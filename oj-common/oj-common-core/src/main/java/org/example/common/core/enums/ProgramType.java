package org.example.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProgramType {

    JAVA(0, "java");

    //拓展部分
//    CPP(1, "C++"),
//    GOLANG(2, "go");

    private Integer value;

    private String desc;

}
