package org.example.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * Date: 2025-01-02
 * Time: 16:49
 */
@Getter
@AllArgsConstructor
public enum UserIdentity {

    ORDINARY (1, "普通用户"),
    ADMIN (2, "管理员");

    private Integer value;

    private String des;

}
