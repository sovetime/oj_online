package org.example.ojfriend.service.question;

import org.example.common.core.domain.TableDataInfo;
import org.example.ojfriend.domain.question.dto.QuestionQueryDTO;
import org.example.ojfriend.domain.question.vo.QuestionDetailVO;
import org.example.ojfriend.domain.question.vo.QuestionVO;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * Date: 2025-01-25
 * Time: 09:43
 */
public interface IQuestionService {
    TableDataInfo list(QuestionQueryDTO questionQueryDTO);

    List<QuestionVO> hotList();

    QuestionDetailVO detail(Long questionId);

    String preQuestion(Long questionId);

    String nextQuestion(Long questionId);
}
