package org.example.ojfriend.mapper.message;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.ojfriend.domain.message.MessageText;
import org.example.ojfriend.domain.message.vo.MessageTextVO;


import java.util.List;

public interface MessageTextMapper extends BaseMapper<MessageText> {

    List<MessageTextVO> selectUserMsgList(Long userId);
}
