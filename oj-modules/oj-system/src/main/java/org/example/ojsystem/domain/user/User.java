package org.example.ojsystem.domain.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Getter;
import lombok.Setter;
import org.example.common.core.domain.BaseEnity;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * Date: 2024-12-30
 * Time: 14:35
 */

@Getter
@Setter
@TableName("tb_user")
public class User extends BaseEnity {

    @JsonSerialize(using = ToStringSerializer.class)
    @TableId(value = "USER_ID", type = IdType.ASSIGN_ID)
    private Long userId;

    private String nickName;

    private String headImage;

    private Integer sex;

    private String phone;

    private String code;

    private String email;

    private String wechat;

    private String schoolName;

    private String majorName;

    private String introduce;

    private Integer status;
}
