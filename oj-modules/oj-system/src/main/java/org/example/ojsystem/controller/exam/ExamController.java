package org.example.ojsystem.controller.exam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.common.core.controller.BaseController;
import org.example.common.core.domain.R;
import org.example.common.core.domain.TableDataInfo;
import org.example.ojsystem.domain.exam.dto.ExamAddDTO;
import org.example.ojsystem.domain.exam.dto.ExamEditDTO;
import org.example.ojsystem.domain.exam.dto.ExamQueryDTO;
import org.example.ojsystem.domain.exam.dto.ExamQuestAddDTO;
import org.example.ojsystem.domain.exam.vo.ExamDetailVO;
import org.example.ojsystem.service.exam.IExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * Date: 2025-01-16
 * Time: 15:31
 */
@RestController
@RequestMapping("/exam")
@Tag(name = "竞赛管理接口")
public class ExamController extends BaseController {

    @Autowired
    private IExamService examService;

    @GetMapping("/list")
    @Operation(summary = "竞赛列表",description = "显示竞赛列表")
    public TableDataInfo list(ExamQueryDTO examQueryDTO) {
        return getTableDataInfo(examService.list(examQueryDTO));
    }

    @PostMapping("/add")
    @Operation(summary = "添加竞赛", description = "添加新的竞赛,需要先判断竞赛标题是否重复，" +
                                    "竞赛开始时间不能早于当前时间，竞赛结束时间不能早当前时间")
    public R<String> add(@RequestBody ExamAddDTO examAddDTO) {
        return R.ok(examService.add(examAddDTO));
    }

    @PostMapping("/question/add")
    @Operation(summary = "竞赛题目添加",description = "竞赛是已经创建出来的，直接判断其状态确认食肉可以进行题目添加操作" +
                                    "在竞赛中添加题目操作核心在于绑定竞赛和题目的关系")
    public R<Void> questionAdd(@RequestBody ExamQuestAddDTO examQuestAddDTO) {
        return toR(examService.questionAdd(examQuestAddDTO));
    }

    @DeleteMapping("/question/delete")
    @Operation(summary = "竞赛题目删除",description = "竞赛是已经创建出来的，直接判断其状态确认食肉可以进行题目删除操作" +
                                    "在竞赛中删除题目操作核心在于删除竞赛和题目的关系")
    public R<Void> questionDelete(Long examId, Long questionId) {
        return toR(examService.questionDelete(examId, questionId));
    }

    @GetMapping("/detail")
    @Operation(summary = "竞赛详细内容",description = "查询出竞赛的名称、起始是将、结束时间、以及用链表存储的题目信息")
    public R<ExamDetailVO> detail(Long examId) {
        return R.ok(examService.detail(examId));
    }

    @PutMapping("/edit")
    @Operation(summary = "编辑竞赛",description = "需要确保你修改的竞赛的名称不能和已经有的竞赛一样，因此引入竞赛id进行条件查询")
    public R<Void> edit(@RequestBody ExamEditDTO examEditDTO) {
        return toR(examService.edit(examEditDTO));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除竞赛",description = "需要保证竞赛是没开始的")
    public R<Void> delete(Long examId) {
        return toR(examService.delete(examId));
    }

    @PutMapping("/publish")
    @Operation(summary = "发布竞赛",description = "竞赛结束之后不能在发布，并且竞赛是有题目的,需要将竞赛缓存到redis中")
    public R<Void> publish(Long examId) {
        return toR(examService.publish(examId));
    }

    @PutMapping("/cancelPublish")
    @Operation(summary = "撤销发布的竞赛",description = "竞赛开始之后不能取消发布，在竞赛结束之后不能取消发布，需要将竞赛从redis中删除")
    public R<Void> cancelPublish(Long examId) {
        return toR(examService.cancelPublish(examId));
    }
}




















