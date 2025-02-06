package org.example.ojfriend.rabbit;

import lombok.extern.slf4j.Slf4j;
import org.example.api.domain.dto.JudgeSubmitDTO;
import org.example.common.core.constants.RabbitMQConstants;
import org.example.common.core.enums.ResultCode;
import org.example.security.exception.ServiceException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JudgeProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    //发送消息
    public void produceMsg(JudgeSubmitDTO judgeSubmitDTO) {
        try {
            rabbitTemplate.convertAndSend(RabbitMQConstants.OJ_WORK_QUEUE, judgeSubmitDTO);
        } catch (Exception e) {
            log.error("生产者发送消息异常", e);
            throw new ServiceException(ResultCode.FAILED_RABBIT_PRODUCE);
        }
    }
}
