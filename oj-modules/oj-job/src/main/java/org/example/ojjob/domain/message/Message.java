package org.example.ojjob.domain.message;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import org.example.common.core.domain.BaseEnity;

@Getter
@Setter
@TableName("tb_message")
public class Message extends BaseEnity {

    @TableId(value = "MESSAGE_ID", type = IdType.ASSIGN_ID)
    private Long messageId;

    private Long textId;

    private Long sendId;//发送者id

    private Long recId;//接收者id
}
