package org.example.ojsystem.controller.sysuser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.common.core.constants.HttpConstants;
import org.example.common.core.controller.BaseController;
import org.example.common.core.domain.R;
import org.example.ojsystem.domain.sysuser.dto.LoginDTO;
import org.example.ojsystem.domain.sysuser.dto.SysUserSaveDTO;
import org.example.common.core.domain.vo.LoginUserVO;
import org.example.ojsystem.domain.sysuser.vo.SysUserVO;
import org.example.ojsystem.service.sysuser.impl.SysUserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * Date: 2024-12-30
 * Time: 14:34
 */

@RequestMapping("/sysUser")
@RestController
@Tag(name="管理员接口")
public class SysUserController extends BaseController {
    @Autowired
    private SysUserServiceImpl sysUserService;

    @PostMapping("/login") //安全性考虑PostMapper
    @Operation(summary = "管理员登录",description = "根据密码进行登录")
    @ApiResponse(responseCode = "1000",description = "操作成功")
    @ApiResponse(responseCode = "2000", description = "服务繁忙请稍后重试")
    @ApiResponse(responseCode = "3102", description = "用户不存在")
    @ApiResponse(responseCode = "3103", description = "用户名或密码错误")
    //返回值，登录成功还是失败,如果失败了说明失败原因
    public R<String> login(@RequestBody LoginDTO loginDTO){
        return sysUserService.login(loginDTO.getUserAccount(), loginDTO.getPassword());
    }

    @GetMapping("/info")
    public R<LoginUserVO> info(@RequestHeader(HttpConstants.AUTHENTICATION) String token){
        return sysUserService.info(token);
    }

    @DeleteMapping("/logout")
    public R<Void> logout(@RequestHeader(HttpConstants.AUTHENTICATION) String token){
        return toR(sysUserService.logout(token));
    }

    @PostMapping("/add")
    @Operation(summary = "新增管理员",description = "根据提供的信息新增管理员")
    @ApiResponse(responseCode = "1000",description = "操作成功")
    @ApiResponse(responseCode = "3101", description = "用户已存在")
    @ApiResponse(responseCode = "2000", description = "服务繁忙请稍后重试")
    public R<Void> add(@RequestBody SysUserSaveDTO sysUserSaveDTO){
        return toR(sysUserService.add(sysUserSaveDTO));
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "删除用户", description = "通过用户id删除用户")
    @Parameters(value = {
            @Parameter(name = "userId", in = ParameterIn.PATH, description = "用户ID")
    })
    @ApiResponse(responseCode = "1000", description = "成功删除用户")
    @ApiResponse(responseCode = "2000", description = "服务繁忙请稍后重试")
    @ApiResponse(responseCode = "3101", description = "用户不存在")
    public R<Void> delete(@PathVariable Long userId) {
        return null;
    }

    @Operation(summary = "用户详情", description = "根据查询条件查询用户详情")
    @GetMapping("/detail")
    @Parameters(value = {
            @Parameter(name = "userId", in = ParameterIn.QUERY, description = "用户ID"),
            @Parameter(name = "sex", in = ParameterIn.QUERY, description = "用户性别")
    })
    @ApiResponse(responseCode = "1000", description = "成功获取用户信息")
    @ApiResponse(responseCode = "2000", description = "服务繁忙请稍后重试")
    @ApiResponse(responseCode = "3101", description = "用户不存在")
    public R<SysUserVO> detail(@RequestParam(required = true) Long userId, @RequestParam(required = false) String sex) {
        return null;
    }

}






















