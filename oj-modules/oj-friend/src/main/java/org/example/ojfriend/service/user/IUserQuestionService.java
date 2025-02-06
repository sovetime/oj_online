package org.example.ojfriend.service.user;


import org.example.api.domain.vo.UserQuestionResultVO;
import org.example.common.core.domain.R;
import org.example.ojfriend.domain.user.dto.UserSubmitDTO;


public interface IUserQuestionService {
    R<UserQuestionResultVO> submit(UserSubmitDTO submitDTO);

    boolean rabbitSubmit(UserSubmitDTO submitDTO);

    UserQuestionResultVO exeResult(Long examId, Long questionId, String currentTime);
}
