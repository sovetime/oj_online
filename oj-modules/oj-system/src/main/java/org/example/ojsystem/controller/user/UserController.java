package org.example.ojsystem.controller.user;

import org.example.common.core.controller.BaseController;
import org.example.common.core.domain.R;
import org.example.common.core.domain.TableDataInfo;
import org.example.ojsystem.domain.user.dto.UserDTO;
import org.example.ojsystem.domain.user.dto.UserQueryDTO;
import org.example.ojsystem.service.user.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * Date: 2024-12-30
 * Time: 14:34
 */
@RestController
@RequestMapping("/user")
public class UserController extends BaseController {

    @Autowired
    private IUserService userService;

    @GetMapping("/list")
    public TableDataInfo list(UserQueryDTO userQueryDTO){
        return getTableDataInfo(userService.list(userQueryDTO));
    }

    @PutMapping("/updateStatus")
    // 拉黑：限制用户操作   解禁：放开对于用户限制
    public R<Void> updateStatus(@RequestBody UserDTO userDTO){
        return toR(userService.updateStatus(userDTO));
    }

}
