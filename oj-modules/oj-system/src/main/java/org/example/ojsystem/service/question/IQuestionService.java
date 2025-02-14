package org.example.ojsystem.service.question;

import org.example.ojsystem.domain.question.dto.QuestionAddDTO;
import org.example.ojsystem.domain.question.dto.QuestionEditDTO;
import org.example.ojsystem.domain.question.dto.QuestionQueryDTO;
import org.example.ojsystem.domain.question.vo.QuestionDetailVO;
import org.example.ojsystem.domain.question.vo.QuestionVO;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * Date: 2025-01-14
 * Time: 21:47
 */
public interface IQuestionService {

    List<QuestionVO> list(QuestionQueryDTO questionQueryDTO);

    boolean add(QuestionAddDTO questionAddDTO);

    QuestionDetailVO detail(Long questionId);

    int edit(QuestionEditDTO questionEditDTO);

    int delete(Long questionId);

}
