package org.example.ojsystem.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.common.core.domain.R;
import org.example.common.core.enums.ResultCode;
import org.example.common.core.enums.UserIdentity;
import org.example.ojsystem.domain.sysuser.SysUser;
import org.example.ojsystem.domain.sysuser.dto.SysUserSaveDTO;
import org.example.ojsystem.mapper.SysUserMapper;
import org.example.ojsystem.service.ISysUserService;
import org.example.ojsystem.utils.BCryptUtils;
import org.example.security.exception.ServiceException;
import org.example.security.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * Date: 2024-12-30
 * Time: 15:48
 */
@Service
//使用 Spring Cloud Config或者其他配置管理工具时，能够动态加载配置文件中的配置信息，不需要重新启动服务
@RefreshScope
public class SysUserServiceImpl implements ISysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private TokenService tokenService;

    @Value("${jwt.secret}")
    private String secret;

    @Override
    public R<String> login(String userAccount, String password) {
        //通过账号查询对应用户信息
        LambdaQueryWrapper<SysUser> queryWrapper=new LambdaQueryWrapper<>();
        //select password from tb_sys_user where user_account = #{userAccount}
        SysUser sysUser=sysUserMapper.selectOne(queryWrapper
                .select(SysUser::getPassword).eq(SysUser::getUserAccount, userAccount));

        //登录校验
        if(sysUser==null){
            return R.fail(ResultCode.FAILED_USER_NOT_EXISTS);
        }
        if(BCryptUtils.matchesPassword(password, sysUser.getPassword())){

            return R.ok(tokenService.createToken(sysUser.getUserId(),
                    secret, UserIdentity.ADMIN.getValue(),sysUser.getNickName(),null));
        }
        return R.fail(ResultCode.FAILED_LOGIN);
    }

    @Override
    public int add(SysUserSaveDTO sysUserSaveDTO) {
        //要考虑重复的情况
        List<SysUser> sysUserList=sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUserAccount, sysUserSaveDTO.getUserAccount()));
        if(CollectionUtil.isNotEmpty(sysUserList)){
            throw new ServiceException(ResultCode.AILED_USER_EXISTS);
        }

        //将dto转化成实体
        SysUser sysUser=new SysUser();
        sysUser.setUserAccount(sysUserSaveDTO.getUserAccount());
        sysUser.setPassword(BCryptUtils.encryptPassword(sysUserSaveDTO.getPassword()));
        return sysUserMapper.insert(sysUser);
    }


    @Override
    public boolean logout(String token) {
        return false;
    }




}













