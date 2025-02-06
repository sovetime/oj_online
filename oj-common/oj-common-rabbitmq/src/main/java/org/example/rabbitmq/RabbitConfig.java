package org.example.rabbitmq;

import org.example.common.core.constants.RabbitMQConstants;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    //创建一个队列
    @Bean
    public Queue workQueue() {
        return new Queue(RabbitMQConstants.OJ_WORK_QUEUE, true);
    }

    //创建一个消息转换器
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
