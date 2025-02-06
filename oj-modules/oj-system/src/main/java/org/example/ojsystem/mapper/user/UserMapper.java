package org.example.ojsystem.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.ojsystem.domain.user.User;
import org.example.ojsystem.domain.user.dto.UserQueryDTO;
import org.example.ojsystem.domain.user.vo.UserVO;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * Date: 2025-01-19
 * Time: 11:03
 */
public interface UserMapper extends BaseMapper<User> {
    List<UserVO> selectUserList(UserQueryDTO userQueryDTO);

}
