package org.example.ojfriend.service.user;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.common.core.domain.R;
import org.example.common.core.domain.vo.LoginUserVO;
import org.example.ojfriend.domain.user.User;
import org.example.ojfriend.domain.user.dto.UserDTO;
import org.example.ojfriend.domain.user.dto.UserUpdateDTO;
import org.example.ojfriend.domain.user.vo.UserVO;
import org.example.ojfriend.mapper.user.UserMapper;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * Date: 2025-01-19
 * Time: 12:31
 */

public interface IUserService {
    boolean sendCode(UserDTO userDTO);

    String codeLogin(String phone,String code);

    boolean logout(String token);

    R<LoginUserVO> info(String token);

    UserVO detail();

    int edit(UserUpdateDTO userUpdateDTO);

    int updateHeadImage(String headImage);
}
