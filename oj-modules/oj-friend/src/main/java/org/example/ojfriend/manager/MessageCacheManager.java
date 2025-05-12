package org.example.ojfriend.manager;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.github.pagehelper.PageHelper;
import org.example.common.core.constants.CacheConstants;
import org.example.common.core.domain.PageQueryDTO;
import org.example.ojfriend.domain.message.vo.MessageTextVO;
import org.example.ojfriend.mapper.message.MessageTextMapper;
import org.example.redis.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * Date: 2025-02-07
 * Time: 16:16
 */
@Component
public class MessageCacheManager {

    @Autowired
    private RedisService redisService;
    @Autowired
    private MessageTextMapper messageTextMapper;

    //刷新缓存
    public void refreshCache(Long userId) {
        List<MessageTextVO> messageTextVOList = messageTextMapper.selectUserMsgList(userId);
        if (CollectionUtil.isEmpty(messageTextVOList)) {
            return;
        }

        List<Long> textIdList = messageTextVOList.stream().map(MessageTextVO::getTextId).toList();
        String userMsgListKey = getUserMsgListKey(userId);
        redisService.rightPushAll(userMsgListKey, textIdList);

        Map<String, MessageTextVO> messageTextVOMap = new HashMap<>();
        for (MessageTextVO messageTextVO : messageTextVOList) {
            messageTextVOMap.put(getMsgDetailKey(messageTextVO.getTextId()), messageTextVO);
        }
        redisService.multiSet(messageTextVOMap);
    }

    //从缓存中获取用户消息列表
    public List<MessageTextVO> getMsgTextVOList(PageQueryDTO dto, Long userId) {
        int start = (dto.getPageNum() - 1) * dto.getPageSize();
        int end = start + dto.getPageSize() - 1;

        String userMsgListKey = getUserMsgListKey(userId);
        List<Long> msgTextIdList = redisService.getCacheListByRange(userMsgListKey, start, end, Long.class);
        //根据textid列表从缓存中获取数据
        List<MessageTextVO> messageTextVOList = assembleMsgTextVOList(msgTextIdList);
        //从数据库中获取数据，并重新刷新缓存
        if (CollectionUtil.isEmpty(messageTextVOList)) {
            messageTextVOList = getMsgTextVOListByDB(dto, userId);
            refreshCache(userId);
        }
        return messageTextVOList;
    }

    //根据textid列表从缓存中获取消息内容
    private List<MessageTextVO> assembleMsgTextVOList(List<Long> msgTextIdList) {
        if (CollectionUtil.isEmpty(msgTextIdList)) {
            return null;
        }

        //拼接redis当中key的方法 并且将拼接好的key存储到一个list中
        List<String> detailKeyList = new ArrayList<>();
        for (Long textId : msgTextIdList) {
            detailKeyList.add(getMsgDetailKey(textId));
        }

        //批量存储消息内容
        List<MessageTextVO> messageTextVOList = redisService.multiGet(detailKeyList, MessageTextVO.class);
        //去除空值
        CollUtil.removeNull(messageTextVOList);
        if (CollectionUtil.isEmpty(messageTextVOList) || messageTextVOList.size() != msgTextIdList.size()) {
            return null;
        }
        return messageTextVOList;
    }

    //从数据库中获取数据
    private List<MessageTextVO> getMsgTextVOListByDB(PageQueryDTO dto, Long userId) {
        PageHelper.startPage(dto.getPageNum(), dto.getPageSize());
        return messageTextMapper.selectUserMsgList(userId);
    }

    public Long getListSize(Long userId) {
        String userMsgListKey = getUserMsgListKey(userId);
        return redisService.getListSize(userMsgListKey);
    }

    private String getUserMsgListKey(Long userId) {
        return CacheConstants.USER_MESSAGE_LIST + userId;
    }

    private String getMsgDetailKey(Long textId) {
        return CacheConstants.MESSAGE_DETAIL + textId;
    }
}
