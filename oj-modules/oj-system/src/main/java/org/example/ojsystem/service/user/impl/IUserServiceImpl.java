package org.example.ojsystem.service.user.impl;

import org.example.common.core.enums.ResultCode;
import org.example.ojsystem.domain.user.User;
import org.example.ojsystem.domain.user.dto.UserDTO;
import org.example.ojsystem.domain.user.dto.UserQueryDTO;
import org.example.ojsystem.domain.user.vo.UserVO;
import org.example.ojsystem.manager.UserCacheManager;
import org.example.ojsystem.mapper.user.UserMapper;
import org.example.ojsystem.service.user.IUserService;
import org.example.security.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * Date: 2025-01-19
 * Time: 11:02
 */
@Service
public class IUserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserCacheManager userCacheManager;

    @Override
    public List<UserVO> list(UserQueryDTO userQueryDTO) {
        return userMapper.selectUserList(userQueryDTO);
    }

    @Override
    public int updateStatus(UserDTO userDTO) {
        User user=userMapper.selectById(userDTO.getUserId());
        if(user==null){
            throw new ServiceException(ResultCode.FAILED_USER_NOT_EXISTS);
        }
        user.setStatus(userDTO.getStatus());
        userCacheManager.updateStatus(user.getUserId(), userDTO.getStatus());
        return userMapper.updateById(user);
    }
}
