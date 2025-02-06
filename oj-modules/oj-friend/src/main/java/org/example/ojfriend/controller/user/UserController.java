package org.example.ojfriend.controller.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.example.common.core.constants.HttpConstants;
import org.example.common.core.controller.BaseController;
import org.example.common.core.domain.R;
import org.example.common.core.domain.vo.LoginUserVO;
import org.example.message.service.AliSmsService;
import org.example.ojfriend.domain.user.dto.UserDTO;
import org.example.ojfriend.domain.user.dto.UserUpdateDTO;
import org.example.ojfriend.domain.user.vo.UserVO;
import org.example.ojfriend.service.user.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * Date: 2025-01-17
 * Time: 22:04
 */

@RestController
@RequestMapping("/user")
public class UserController extends BaseController {
    @Autowired
    private IUserService userService;

    @PostMapping("/sendCode")
    public R<Void> sendCode(@RequestBody UserDTO userDTO) {
        return toR(userService.sendCode(userDTO)) ;
    }

    @PostMapping("/code/login")
    @Operation(summary = "用户登录",description = "需要先获取验证码才能登录" +
            "默认验证码是123456，这个登录也要调用发送信息的方法，因为验证码是缓存在redis中的，登录成功之后会删除缓存中的验证码")
    public R<String> codeLogin(@RequestBody UserDTO userDTO) {
        return R.ok(userService.codeLogin(userDTO.getPhone(), userDTO.getCode()));
    }

    @DeleteMapping("/logout")
    public R<Void> logout(@RequestHeader(HttpConstants.AUTHENTICATION) String token) {
        return toR(userService.logout(token));
    }

    @GetMapping("/info")
    public R<LoginUserVO> info(@RequestHeader(HttpConstants.AUTHENTICATION) String token) {
        return userService.info(token);
    }

    @GetMapping("/detail")
    public R<UserVO> detail() {
        return R.ok(userService.detail());
    }

    @PutMapping("/edit")
    public R<Void> edit(@RequestBody UserUpdateDTO userUpdateDTO) {
        return toR(userService.edit(userUpdateDTO));
    }

    @PutMapping("/head-image/update")
    public R<Void> updateHeadImage(@RequestBody UserUpdateDTO userUpdateDTO) {
        return toR(userService.updateHeadImage(userUpdateDTO.getHeadImage()));
    }
}
