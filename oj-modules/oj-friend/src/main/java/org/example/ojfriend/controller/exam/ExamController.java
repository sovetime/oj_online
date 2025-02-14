package org.example.ojfriend.controller.exam;


import io.swagger.v3.oas.annotations.Operation;
import org.example.common.core.controller.BaseController;
import org.example.common.core.domain.R;
import org.example.common.core.domain.TableDataInfo;
import org.example.ojfriend.domain.exam.dto.ExamQueryDTO;
import org.example.ojfriend.domain.exam.dto.ExamRankDTO;
import org.example.ojfriend.service.exam.IExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/exam")
public class ExamController extends BaseController {

    @Autowired
    private IExamService examService;

    @GetMapping("/semiLogin/list")
    @Operation(summary = "竞赛列表",description="直接访问到数据库的")
    public TableDataInfo list(ExamQueryDTO examQueryDTO) {
        return getTableDataInfo(examService.list(examQueryDTO));
    }

    @GetMapping("/semiLogin/redis/list")
    @Operation(summary = "缓存的竞赛列表",description = "还在进行中的竞赛列表，历史竞赛列表会通过xxl-job定时刷新"+
                    "将状态为已发布的竞赛缓存在redis中，提升处理请求的速度" +
                    "未登录状态下也是可以看见相对应的信息的，因此后面设查询的userId传null就可以了" +
                    "其中包含检测是否有当前用户参加的竞赛，并进行状态设置")
    public TableDataInfo redisList(ExamQueryDTO examQueryDTO) {
        return examService.redisList(examQueryDTO);
    }

    @GetMapping("/rank/list")
    @Operation(summary = "竞赛排名列表")
    public TableDataInfo rankList(ExamRankDTO examRankDTO) {
        return examService.rankList(examRankDTO);
    }

    @GetMapping("/getFirstQuestion")
    @Operation(summary = "获取竞赛的第一个题目")
    public R<String> getFirstQuestion(Long examId) {
        return R.ok(examService.getFirstQuestion(examId));
    }

    @GetMapping("/preQuestion")
    @Operation(summary = "获取前一个题目")
    public R<String> preQuestion(Long examId, Long questionId) {
        return R.ok(examService.preQuestion(examId, questionId));
    }

    @GetMapping("/nextQuestion")
    @Operation(summary = "获取下一个题目")
    public R<String> nextQuestion(Long examId, Long questionId) {
        return R.ok(examService.nextQuestion(examId, questionId));
    }
}
