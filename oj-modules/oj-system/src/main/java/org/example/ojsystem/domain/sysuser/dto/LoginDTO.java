package org.example.ojsystem.domain.sysuser.dto;

import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * Date: 2024-12-30
 * Time: 17:18
 */

@Data
public class LoginDTO {

    private String userAccount;

    private String password;
}
