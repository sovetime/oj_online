package org.example.common.core.domain;

import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * Date: 2025-01-02
 * Time: 15:41
 */
@Data
public class LoginUser {
    private String nickName; //用户昵称

    private Integer identity;  //1  表示普通用户  2 ： 表示管理员用户

    private String headImage;  //头像

}
