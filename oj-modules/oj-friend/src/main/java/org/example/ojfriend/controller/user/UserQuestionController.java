package org.example.ojfriend.controller.user;

import io.swagger.v3.oas.annotations.Operation;
import org.example.api.domain.vo.UserQuestionResultVO;
import org.example.common.core.controller.BaseController;
import org.example.common.core.domain.R;
import org.example.ojfriend.domain.user.UserSubmit;
import org.example.ojfriend.domain.user.dto.UserSubmitDTO;
import org.example.ojfriend.service.user.IUserQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * Date: 2025-01-28
 * Time: 09:20
 */
@RestController
@RequestMapping("user/question")
public class UserQuestionController extends BaseController {

    @Autowired
    private IUserQuestionService userQuestionService;

    @PostMapping("/submit")
    @Operation(summary = "用户代码提交", description = "这里需要做语言的类型校验")
    public R<UserQuestionResultVO> submit(@RequestBody UserSubmitDTO submitDTO) {
        return userQuestionService.submit(submitDTO);
    }

    @PostMapping("/rabbit/submit")
    @Operation(summary = "用户代码提交mq版本",description = "对之前的方法调用进行了优化" +
            "只需要把消息发送给队列就可以了，judge会接收消息并解决判题逻辑")
    public R<Void> rabbitSubmit(@RequestBody UserSubmitDTO submitDTO) {
        return toR(userQuestionService.rabbitSubmit(submitDTO));
    }

    @GetMapping("/exe/result")
    public  R<UserQuestionResultVO> exeResult(Long examId, Long questionId, String currentTime) {
        return R.ok(userQuestionService.exeResult(examId, questionId, currentTime));
    }
}




