package org.example.ojfriend.service.user.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.example.common.core.constants.Constants;
import org.example.common.core.domain.PageQueryDTO;
import org.example.common.core.domain.TableDataInfo;
import org.example.common.core.utils.ThreadLocalUtil;
import org.example.ojfriend.domain.message.vo.MessageTextVO;
import org.example.ojfriend.manager.MessageCacheManager;
import org.example.ojfriend.mapper.message.MessageTextMapper;
import org.example.ojfriend.service.user.IUserMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class UserMessageServiceImpl implements IUserMessageService {

    @Autowired
    private MessageCacheManager messageCacheManager;
    @Autowired
    private MessageTextMapper messageTextMapper;

    @Override
    public TableDataInfo list(PageQueryDTO dto) {
        Long userId = ThreadLocalUtil.get(Constants.USER_ID, Long.class);
        //从缓存中获取个人参加的竞赛列表
        Long total = messageCacheManager.getListSize(userId);
        List<MessageTextVO> messageTextVOList;
        if (total == null || total <= 0) {
            //从数据库中查询我的竞赛列表
            PageHelper.startPage(dto.getPageNum(), dto.getPageSize());
            messageTextVOList = messageTextMapper.selectUserMsgList(userId);
            messageCacheManager.refreshCache(userId);
            total = new PageInfo<>(messageTextVOList).getTotal();
        } else {
            //从缓存中获取消息列表
            messageTextVOList = messageCacheManager.getMsgTextVOList(dto, userId);
        }

        if (CollectionUtil.isEmpty(messageTextVOList)) {
            return TableDataInfo.empty();
        }
        return TableDataInfo.success(messageTextVOList, total);
    }
}
