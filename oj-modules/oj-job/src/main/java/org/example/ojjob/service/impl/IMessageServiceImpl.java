package org.example.ojjob.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.ojjob.domain.message.Message;
import org.example.ojjob.mapper.message.MessageMapper;
import org.example.ojjob.service.IMessageService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * Date: 2025-02-07
 * Time: 11:12
 */

//mybits-plus提供批量插入操作，extends ServiceImpl<MessageMapper, Message>，调用saveBatch
@Service
public class IMessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements IMessageService {

    @Override
    public boolean batchInsert(List<Message> messageTextList) {
        return saveBatch(messageTextList);
    }
}
