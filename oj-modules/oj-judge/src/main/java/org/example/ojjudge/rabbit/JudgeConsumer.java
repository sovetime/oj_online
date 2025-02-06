package org.example.ojjudge.rabbit;

import lombok.extern.slf4j.Slf4j;
import org.example.api.domain.dto.JudgeSubmitDTO;
import org.example.common.core.constants.RabbitMQConstants;
import org.example.ojjudge.service.IJudgeService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JudgeConsumer {

    @Autowired
    private IJudgeService judgeService;

    @RabbitListener(queues = RabbitMQConstants.OJ_WORK_QUEUE)
    public void consume(JudgeSubmitDTO judgeSubmitDTO) {
        log.info("收到消息为: {}", judgeSubmitDTO);
        judgeService.doJudgeJavaCode(judgeSubmitDTO);
    }
}
