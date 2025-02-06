package org.example.ojjudge.service;

import org.example.api.domain.dto.JudgeSubmitDTO;
import org.example.api.domain.vo.UserQuestionResultVO;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * Date: 2025-01-29
 * Time: 22:49
 */
public interface IJudgeService {

    UserQuestionResultVO doJudgeJavaCode(JudgeSubmitDTO judgeSubmitDTO);
}
