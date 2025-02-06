package org.example.api;


import org.example.api.domain.dto.JudgeSubmitDTO;
import org.example.api.domain.vo.UserQuestionResultVO;
import org.example.common.core.constants.Constants;
import org.example.common.core.domain.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(contextId = "RemoteJudgeService", value = Constants.JUDGE_SERVICE)
public interface RemoteJudgeService {

    @PostMapping("/judge/doJudgeJavaCode")
    R<UserQuestionResultVO> doJudgeJavaCode(@RequestBody JudgeSubmitDTO judgeSubmitDTO);
}
