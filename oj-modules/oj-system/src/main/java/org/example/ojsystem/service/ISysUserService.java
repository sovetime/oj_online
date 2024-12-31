package org.example.ojsystem.service;

import org.example.common.core.domain.R;
import org.example.ojsystem.domain.sysuser.dto.SysUserSaveDTO;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * Date: 2024-12-30
 * Time: 15:48
 */

public interface ISysUserService {

    R<Void> login(String userAcount, String password);

    boolean logout(String token);

    int add(SysUserSaveDTO sysUserSaveDTO);
}
