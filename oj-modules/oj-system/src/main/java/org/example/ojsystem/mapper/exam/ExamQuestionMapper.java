package org.example.ojsystem.mapper.exam;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.ojsystem.domain.exam.ExamQuestion;
import org.example.ojsystem.domain.question.vo.QuestionVO;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * Date: 2025-01-16
 * Time: 15:30
 */
public interface ExamQuestionMapper extends BaseMapper<ExamQuestion> {
    List<QuestionVO> selectExamQuestionList(Long examId);

}
