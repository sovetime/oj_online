package org.example.ojfriend.service.exam;

import org.example.common.core.domain.TableDataInfo;
import org.example.ojfriend.domain.exam.dto.ExamQueryDTO;
import org.example.ojfriend.domain.exam.vo.ExamVO;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * Date: 2025-01-21
 * Time: 13:19
 */
public interface IExamService {
    List<ExamVO> list(ExamQueryDTO examQueryDTO);

    TableDataInfo redisList(ExamQueryDTO examQueryDTO);

    String getFirstQuestion(Long examId);

    String preQuestion(Long examId, Long questionId);

    String nextQuestion(Long examId, Long questionId);
}
