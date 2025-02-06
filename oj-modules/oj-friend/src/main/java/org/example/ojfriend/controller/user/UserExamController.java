package org.example.ojfriend.controller.user;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import org.example.common.core.constants.HttpConstants;
import org.example.common.core.controller.BaseController;
import org.example.common.core.domain.R;
import org.example.common.core.domain.TableDataInfo;
import org.example.ojfriend.aspect.CheckUserStatus;
import org.example.ojfriend.domain.exam.dto.ExamDTO;
import org.example.ojfriend.domain.exam.dto.ExamQueryDTO;
import org.example.ojfriend.service.user.IUserExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/exam")
public class UserExamController extends BaseController {

    @Autowired
    private IUserExamService userExamService;

    @CheckUserStatus//验证用户状态
    @PostMapping("/enter")
    @Operation(summary = "报名参加竞赛",description = "这里没有进行竞赛的状态转换，直接将添加的竞赛存入缓存中" +
            "获取参加的竞赛列表的时候从缓存中获取"+
            "用户在拉黑状态不可以报名参加竞赛")
    public R<Void> enter(@RequestHeader(HttpConstants.AUTHENTICATION) String token, @RequestBody ExamDTO examDTO) {
        return toR(userExamService.enter(token, examDTO.getExamId()));
    }

    @GetMapping("/list")
    @Operation(summary = "用户参加竞赛列表")
    public TableDataInfo list(ExamQueryDTO examQueryDTO) {
        return userExamService.list(examQueryDTO);
    }


}
