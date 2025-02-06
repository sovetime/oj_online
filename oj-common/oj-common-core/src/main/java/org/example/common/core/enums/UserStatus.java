package org.example.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
public enum UserStatus {

    Block(0),//拉黑状态

    Normal(1);//普通状态

    private Integer value;

}
