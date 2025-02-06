package org.example.ojfriend.controller.question;


import io.swagger.v3.oas.annotations.Operation;
import org.example.common.core.controller.BaseController;
import org.example.common.core.domain.R;
import org.example.common.core.domain.TableDataInfo;
import org.example.ojfriend.domain.question.dto.QuestionQueryDTO;
import org.example.ojfriend.domain.question.vo.QuestionDetailVO;
import org.example.ojfriend.domain.question.vo.QuestionVO;
import org.example.ojfriend.service.question.IQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/question")
public class QuestionController extends BaseController {

    @Autowired
    private IQuestionService questionService;

    @GetMapping("/semiLogin/list")
    @Operation(summary = "获取题目列表",description = "从ES题库中获取数据，ES题库没有数据会把数据库中的数据保存到ES题库中" +
            "再进行题目列表的获取和查询")
    public TableDataInfo list(QuestionQueryDTO questionQueryDTO) {
        return questionService.list(questionQueryDTO);
    }

    @GetMapping("/semiLogin/hotList")
    public R<List<QuestionVO>> hotList() {
        return R.ok(questionService.hotList());
    }

    @GetMapping("/detail")
    public R<QuestionDetailVO> detail(Long questionId) {
        return R.ok(questionService.detail(questionId));
    }

    @GetMapping("/preQuestion")
    @Operation(summary = "前一道题目",description = "用户答题过程中切换题目部分")
    public R<String> preQuestion(Long questionId) {
        return R.ok(questionService.preQuestion(questionId));
    }

    @GetMapping("/nextQuestion")
    @Operation(summary = "后一道题目")
    public R<String> nextQuestion(Long questionId) {
        return R.ok(questionService.nextQuestion(questionId));
    }
}
