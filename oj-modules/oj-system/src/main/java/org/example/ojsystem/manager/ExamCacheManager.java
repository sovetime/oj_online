package org.example.ojsystem.manager;


import org.example.common.core.constants.CacheConstants;
import org.example.ojsystem.domain.exam.Exam;
import org.example.redis.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ExamCacheManager {

    @Autowired
    private RedisService redisService;

    //缓存竞赛信息
    public void addCache(Exam exam) {
        //将考试ID添加到还在进行中的试列表
        redisService.leftPushForList(getExamListKey(), exam.getExamId());
        redisService.setCacheObject(getDetailKey(exam.getExamId()), exam);
    }

    //从缓存中删除指定竞赛的相关信息
    public void deleteCache(Long examId) {
        //从未完成考试列表中移除该考试ID
        redisService.removeForList(getExamListKey(), examId);
        redisService.deleteObject(getDetailKey(examId));
        redisService.deleteObject(getExamQuestionListKey(examId));
    }

    //还在进行中的考试列表
    private String getExamListKey() {
        return CacheConstants.EXAM_UNFINISHED_LIST;
    }

    //考试详情
    private String getDetailKey(Long examId) {
        return CacheConstants.EXAM_DETAIL + examId;
    }

    //考试题目列表
    private String getExamQuestionListKey(Long examId) {
        return CacheConstants.EXAM_QUESTION_LIST + examId;
    }
}
