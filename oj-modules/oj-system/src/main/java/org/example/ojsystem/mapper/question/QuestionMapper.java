package org.example.ojsystem.mapper.question;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.ojsystem.domain.question.Question;
import org.example.ojsystem.domain.question.dto.QuestionQueryDTO;
import org.example.ojsystem.domain.question.vo.QuestionVO;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * Date: 2025-01-14
 * Time: 21:48
 */
public interface QuestionMapper extends BaseMapper<Question> {

    List<QuestionVO> selectQuestionList(QuestionQueryDTO questionQueryDTO);

}
