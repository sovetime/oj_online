package org.example.ojsystem.service.user;

import org.example.common.core.domain.R;
import org.example.ojsystem.domain.user.dto.UserDTO;
import org.example.ojsystem.domain.user.dto.UserQueryDTO;
import org.example.ojsystem.domain.user.vo.UserVO;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * Date: 2025-01-19
 * Time: 11:02
 */
public interface IUserService {
    List<UserVO> list(UserQueryDTO userQueryDTO);

    int updateStatus(UserDTO userDTO);
}
