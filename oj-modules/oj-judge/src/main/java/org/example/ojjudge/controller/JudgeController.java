package org.example.ojjudge.controller;

import org.example.api.domain.dto.JudgeSubmitDTO;
import org.example.api.domain.vo.UserQuestionResultVO;
import org.example.common.core.controller.BaseController;
import org.example.common.core.domain.R;
import org.example.ojjudge.service.IJudgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * Date: 2025-01-29
 * Time: 22:47
 */
@RestController
@RequestMapping("/judge")
public class JudgeController extends BaseController {

    @Autowired
    private IJudgeService iJudgeService;

    @PostMapping("/doJudgeJavaCode")
    public R<UserQuestionResultVO> doJudgeJavaCode(@RequestBody JudgeSubmitDTO judgeSubmitDTO) {
        return R.ok(iJudgeService.doJudgeJavaCode(judgeSubmitDTO));
    }
}
