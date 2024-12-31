package org.example.ojsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.common.core.domain.R;
import org.example.common.core.enums.ResultCode;
import org.example.ojsystem.domain.sysuser.SysUser;
import org.example.ojsystem.domain.sysuser.dto.SysUserSaveDTO;
import org.example.ojsystem.mapper.SysUserMapper;
import org.example.ojsystem.service.ISysUserService;
import org.example.ojsystem.utils.BCryptUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * Created with IntelliJ IDEA.
 * Description:
 * Date: 2024-12-30
 * Time: 15:48
 */
@Service
public class SysUserServiceImpl implements ISysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public R<Void> login(String userAcount, String password) {
        int a=10/0;
        //通过账号查询对应用户信息
        LambdaQueryWrapper<SysUser> queryWrapper=new LambdaQueryWrapper<>();
        //select password from tb_sys_user where user_account = #{userAccount}
        SysUser sysUser=sysUserMapper.selectOne(queryWrapper
                .select(SysUser::getPassword).eq(SysUser::getUserAccount, userAcount));

        //登录校验
        if(sysUser==null){
            return R.fail(ResultCode.FAILED_USER_NOT_EXISTS);
        }
        if(BCryptUtils.matchesPassword(password, sysUser.getPassword())){
            return R.ok();
        }
        return R.fail(ResultCode.FAILED_LOGIN);
    }

    @Override
    public boolean logout(String token) {
        return false;
    }

    @Override
    public int add(SysUserSaveDTO sysUserSaveDTO) {
        return 0;
    }




//    @Override
//    public R<Void> add(String userAccount, String password) {
//
//        return null;
//    }

}













