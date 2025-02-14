package org.example.ojsystem.controller.question;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.common.core.controller.BaseController;
import org.example.common.core.domain.R;
import org.example.common.core.domain.TableDataInfo;
import org.example.ojsystem.domain.question.dto.QuestionAddDTO;
import org.example.ojsystem.domain.question.dto.QuestionEditDTO;
import org.example.ojsystem.domain.question.dto.QuestionQueryDTO;
import org.example.ojsystem.domain.question.vo.QuestionDetailVO;
import org.example.ojsystem.service.question.IQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Created with IntelliJ IDEA.
 * Description:QuestionQueryDTO
 * Date: 2025-01-14
 * Time: 21:44
 */
@RestController
@RequestMapping("/question")
@Tag(name = "题目管理接口")
public class QuestionController extends BaseController {
    @Autowired
    private IQuestionService questionService;

    @GetMapping("/list")
    @Operation(summary = "题目列表",description = "显示题目列表")
    public TableDataInfo list(QuestionQueryDTO questionQueryDTO) {
        return getTableDataInfo(questionService.list(questionQueryDTO));
    }

    @PostMapping("/add")
    @Operation(summary = "新增题目",description = "登录之后才能进行添加")
    public R<Void> add(@RequestBody QuestionAddDTO questionAddDTO) {
        return toR(questionService.add(questionAddDTO));
    }

    @Operation(summary = "显示题目详细信息",description = "获取题目的详细信息")
    @GetMapping("/detail")
    public R<QuestionDetailVO> detail(Long questionId) {
        return R.ok(questionService.detail(questionId));
    }

    @PutMapping("/edit")
    @Operation(summary = "修改题目信息题目",description = "对题目内容进行修改后执行相应逻辑")
    public R<Void> edit(@RequestBody QuestionEditDTO questionEditDTO) {
        return toR(questionService.edit(questionEditDTO));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除题目",description = "根据题目id删除题目")
    public R<Void> delete(Long questionId) {
        return toR(questionService.delete(questionId));
    }

}














