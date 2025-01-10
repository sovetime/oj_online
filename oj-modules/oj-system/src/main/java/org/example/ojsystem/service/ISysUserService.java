package org.example.ojsystem.service;

import org.example.common.core.domain.R;
import org.example.ojsystem.domain.sysuser.dto.SysUserSaveDTO;
import org.springframework.stereotype.Service;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * Date: 2024-12-30
 * Time: 15:48
 */

public interface ISysUserService {

    R<String> login(String userAcount, String password);

    int add(SysUserSaveDTO sysUserSaveDTO);

    boolean logout(String token);

}
