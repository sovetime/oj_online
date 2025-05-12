package org.example.ojjob.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.ojjob.domain.message.MessageText;
import org.example.ojjob.mapper.message.MessageTextMapper;
import org.example.ojjob.service.IMessageTextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * Date: 2025-02-07
 * Time: 11:12
 */


@Service
public class IMessageTestServiceImpl extends ServiceImpl<MessageTextMapper, MessageText> implements IMessageTextService {

    @Override
    public boolean batchInsert(List<MessageText> messageTextList) {
        return saveBatch(messageTextList);
    }
}
