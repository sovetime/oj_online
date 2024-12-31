package org.example.ojsystem.domain.sysuser.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * Date: 2024-12-30
 * Time: 22:57
 */
@Data
public class SysUserVO {
    @Schema(description = "用户账号")
    private String userAccount;

    @Schema(description = "用户昵称")
    private String nickName;
}
