package org.example.ojfriend.manager;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.common.core.constants.CacheConstants;
import org.example.common.core.enums.ResultCode;
import org.example.ojfriend.domain.question.Question;
import org.example.ojfriend.mapper.question.QuestionMapper;
import org.example.redis.service.RedisService;
import org.example.security.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class QuestionCacheManager {

    @Autowired
    private RedisService redisService;
    @Autowired
    private QuestionMapper questionMapper;

    //获取题目列表长度
    public Long getListSize() {
        return redisService.getListSize(CacheConstants.QUESTION_LIST);
    }

    //获取热门题目列表长度
    public Long getHostListSize() {
        return redisService.getListSize(CacheConstants.QUESTION_HOST_LIST);
    }

    //刷新题目列表缓存
    public void refreshCache() {
        List<Question> questionList = questionMapper.selectList(new LambdaQueryWrapper<Question>()
                .select(Question::getQuestionId).orderByDesc(Question::getCreateTime));
        if (CollectionUtil.isEmpty(questionList)) {
            return;
        }
        List<Long> questionIdList = questionList.stream().map(Question::getQuestionId).toList();
        redisService.rightPushAll(CacheConstants.QUESTION_LIST, questionIdList);
    }

    //获取上一个题目的ID
    public Long preQuestion(Long questionId) {
        //获取当前题目在列表中的索引
        Long index = redisService.indexOfForList(CacheConstants.QUESTION_LIST, questionId);
        if (index == 0) {
            throw new ServiceException(ResultCode.FAILED_FIRST_QUESTION);
        }
        return redisService.indexForList(CacheConstants.QUESTION_LIST, index - 1, Long.class);
    }

    //获取下一个题目的ID
    public Object nextQuestion(Long questionId) {
        Long index = redisService.indexOfForList(CacheConstants.QUESTION_LIST, questionId);
        long lastIndex = getListSize() - 1;
        if (index == lastIndex) {
            throw new ServiceException(ResultCode.FAILED_LAST_QUESTION);
        }
        return redisService.indexForList(CacheConstants.QUESTION_LIST, index + 1, Long.class);
    }

    public List<Long> getHostList() {
        return redisService.getCacheListByRange(CacheConstants.QUESTION_HOST_LIST,
                CacheConstants.DEFAULT_START, CacheConstants.DEFAULT_END, Long.class);
    }

    public void refreshHotQuestionList(List<Long> hotQuestionIdList) {
        if (CollectionUtil.isEmpty(hotQuestionIdList)) {
            return;
        }
        redisService.rightPushAll(CacheConstants.QUESTION_HOST_LIST, hotQuestionIdList);
    }
}
