package org.example.ojsystem.service.exam;

import org.example.ojsystem.domain.exam.dto.ExamAddDTO;
import org.example.ojsystem.domain.exam.dto.ExamEditDTO;
import org.example.ojsystem.domain.exam.dto.ExamQueryDTO;
import org.example.ojsystem.domain.exam.dto.ExamQuestAddDTO;
import org.example.ojsystem.domain.exam.vo.ExamDetailVO;
import org.example.ojsystem.domain.exam.vo.ExamVO;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * Date: 2025-01-16
 * Time: 15:27
 */
public interface IExamService {
    List<ExamVO> list(ExamQueryDTO examQueryDTO);

    String add(ExamAddDTO examAddDTO);

    boolean questionAdd(ExamQuestAddDTO examQuestAddDTO);

    int questionDelete(Long examId, Long questionId);

    ExamDetailVO detail(Long examId);

    int edit(ExamEditDTO examEditDTO);

    int delete(Long examId);

    int publish(Long examId);

    int cancelPublish(Long examId);
}
