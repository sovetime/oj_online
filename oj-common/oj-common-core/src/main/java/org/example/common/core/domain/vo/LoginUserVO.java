package org.example.common.core.domain.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * Date: 2025-01-11
 * Time: 21:18
 */
@Getter
@Setter
public class LoginUserVO {

    private String nickName; //用户昵称,前端页面需要根据nickName 确认当前身份

    private String headImage;  //头像

}
