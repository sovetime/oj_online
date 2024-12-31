package org.example.ojsystem.domain.sysuser.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * Date: 2024-12-30
 * Time: 19:11
 */
@Data
public class SysUserSaveDTO {
    @Schema(description = "用户账号")
    private String userAccount;
    @Schema(description = "用户密码")
    private String password;
}
