package org.example.ojjudge.rabbit;

import lombok.extern.slf4j.Slf4j;
import org.example.api.domain.dto.JudgeSubmitDTO;
import org.example.common.core.constants.RabbitMQConstants;
import org.example.common.core.enums.ResultCode;
import org.example.ojjudge.service.IJudgeService;
import org.example.security.exception.ServiceException;
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
        try{
            log.info("收到消息为: {}", judgeSubmitDTO);
            judgeService.doJudgeJavaCode(judgeSubmitDTO);
        }catch (Exception e){
            throw new RuntimeException("处理消息失败: " + e.getMessage(), e);
        }
    }
}
