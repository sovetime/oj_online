package org.example.ojsystem.service.sysuser;

import org.example.common.core.domain.R;
import org.example.common.core.domain.vo.LoginUserVO;
import org.example.ojsystem.domain.sysuser.dto.SysUserSaveDTO;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * Date: 2024-12-30
 * Time: 15:48
 */

public interface ISysUserService {

    R<String> login(String userAcount, String password);

    int add(SysUserSaveDTO sysUserSaveDTO);

    R<LoginUserVO> info(String token);

    boolean logout(String token);

}
