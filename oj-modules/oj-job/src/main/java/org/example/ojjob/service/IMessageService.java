package org.example.ojjob.service;


import org.example.ojjob.domain.message.Message;

import java.util.List;

public interface IMessageService {

    boolean batchInsert(List<Message> messageTextList);
}
