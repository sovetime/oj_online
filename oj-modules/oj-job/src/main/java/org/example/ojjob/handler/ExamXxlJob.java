package org.example.ojjob.handler;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.example.common.core.constants.CacheConstants;
import org.example.common.core.constants.Constants;
import org.example.ojjob.domain.exam.Exam;
import org.example.ojjob.mapper.exam.ExamMapper;
import org.example.redis.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* Created with IntelliJ IDEA.
* Description:
* Date: 2025-01-22
* Time: 21:08
*/
@Component
@Slf4j
public class ExamXxlJob {

    @Autowired
    private RedisService redisService;
    @Autowired
    private ExamMapper examMapper;

    //我们定为每天早上8点更新，cron表表达式是0 0 8 * * ?
    @XxlJob("examListOrganizeHandler")
    public void examListOrganizeHandler() {
        // 统计哪些竞赛应该存入还在进行的列表中，哪些竞赛应该存入历史竞赛列表中，统计出来了之后再存入对应的缓存中
        log.info("*** examListOrganizeHandler ***");
        //未完成竞赛列表
        List<Exam> unFinishList = examMapper.selectList(new LambdaQueryWrapper<Exam>()
                .select(Exam::getExamId, Exam::getTitle, Exam::getStartTime, Exam::getEndTime)
                .gt(Exam::getEndTime, LocalDateTime.now())
                .eq(Exam::getStatus, Constants.TRUE)
                .orderByDesc(Exam::getCreateTime));
        refreshCache(unFinishList, CacheConstants.EXAM_UNFINISHED_LIST);

        //历史竞赛列表
        List<Exam> historyList = examMapper.selectList(new LambdaQueryWrapper<Exam>()
                .select(Exam::getExamId, Exam::getTitle, Exam::getStartTime, Exam::getEndTime)
                .le(Exam::getEndTime, LocalDateTime.now())
                .eq(Exam::getStatus, Constants.TRUE)
                .orderByDesc(Exam::getCreateTime));
        refreshCache(historyList, CacheConstants.EXAM_HISTORY_LIST);
        log.info("*** examListOrganizeHandler 统计结束 ***");
    }

    //刷新缓存
    public void refreshCache(List<Exam> examList, String examListKey) {
        if (CollectionUtil.isEmpty(examList)) {
            return;
        }

        Map<String, Exam> examMap = new HashMap<>();
        List<Long> examIdList = new ArrayList<>();
        for (Exam exam : examList) {
            examMap.put(getDetailKey(exam.getExamId()), exam);
            examIdList.add(exam.getExamId());
        }

        redisService.multiSet(examMap);
        redisService.deleteObject(examListKey);
        redisService.rightPushAll(examListKey, examIdList);      //尾插
    }

    private String getDetailKey(Long examId) {
        return CacheConstants.EXAM_DETAIL + examId;
    }

    private String getUserMsgListKey(Long userId) {
        return CacheConstants.USER_MESSAGE_LIST + userId;
    }

    private String getMsgDetailKey(Long textId) {
        return CacheConstants.MESSAGE_DETAIL + textId;
    }

    private String getExamRankListKey(Long examId) {
        return CacheConstants.EXAM_RANK_LIST + examId;
    }

}

