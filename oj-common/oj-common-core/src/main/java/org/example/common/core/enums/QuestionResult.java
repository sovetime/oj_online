package org.example.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum QuestionResult {

    ERROR(0),
    PASS(1);

    private Integer value;

}
