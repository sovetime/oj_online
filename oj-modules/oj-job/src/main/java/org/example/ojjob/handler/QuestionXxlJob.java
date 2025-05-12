package org.example.ojjob.handler;

import cn.hutool.core.collection.CollectionUtil;
import com.github.pagehelper.PageHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.example.common.core.constants.CacheConstants;
import org.example.common.core.constants.Constants;
import org.example.ojjob.mapper.user.UserSubmitMapper;
import org.example.redis.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * Date: 2025-02-07
 * Time: 11:08
 */

@Component
@Slf4j
public class QuestionXxlJob {
    @Autowired
    private UserSubmitMapper userSubmitMapper;
    @Autowired
    private RedisService redisService;

    @XxlJob("questionListOrganizeHandler")
    public void questionListOrganizeHandler(){
        log.info("----- 题目热门列表统计开始 ------");
        PageHelper.startPage(Constants.HOST_QUESTION_LIST_START, Constants.HOST_QUESTION_LIST_END);
        //更新热门题目
        List<Long> questionIdList = userSubmitMapper.selectHostQuestionList();
        if (CollectionUtil.isEmpty(questionIdList)) {
            return;
        }

        redisService.deleteObject(CacheConstants.QUESTION_HOST_LIST);
        redisService.rightPushAll(CacheConstants.QUESTION_HOST_LIST, questionIdList);
        log.info("----- 题目热门列表统计结束 ------");
    }
}
