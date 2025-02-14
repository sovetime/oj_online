package org.example.ojjob.handler;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.example.common.core.constants.CacheConstants;
import org.example.common.core.constants.Constants;
import org.example.common.core.utils.ThreadLocalUtil;
import org.example.ojjob.domain.exam.Exam;
import org.example.ojjob.domain.message.Message;
import org.example.ojjob.domain.message.MessageText;
import org.example.ojjob.domain.message.vo.MessageTextVO;
import org.example.ojjob.domain.user.UserScore;
import org.example.ojjob.mapper.exam.ExamMapper;
import org.example.ojjob.mapper.message.MessageTextMapper;
import org.example.ojjob.mapper.user.UserExamMapper;
import org.example.ojjob.mapper.user.UserSubmitMapper;
import org.example.ojjob.service.IMessageService;
import org.example.ojjob.service.IMessageTextService;
import org.example.redis.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    @Autowired
    private IMessageTextService messageTextService;
    @Autowired
    private IMessageService messageService;
    @Autowired
    private UserSubmitMapper userSubmitMapper;
    @Autowired
    private MessageTextMapper messageTextMapper;
    @Autowired
    private UserExamMapper userExamMapper;

    //我们定为每天凌晨1点更新，cron表表达式是0 0 1 * * ?
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

    //竞赛排名
    @XxlJob("examResultHandler")
    public void examResultHandler() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime minusDateTime = now.minusDays(1);// 获取前一天的时间
        //查询前一天你结束的竞赛
        List<Exam> examList = examMapper.selectList(new LambdaQueryWrapper<Exam>()
                .select(Exam::getExamId, Exam::getTitle)
                .eq(Exam::getStatus, Constants.TRUE)
                .ge(Exam::getEndTime, minusDateTime)
                .le(Exam::getEndTime, now));
        if (CollectionUtil.isEmpty(examList)) {
            return;
        }

        //获取前一天结束竞赛的id
        Set<Long> examIdSet = examList.stream().map(Exam::getExamId).collect(Collectors.toSet());
        //获取前一天结束竞赛中用户排名信息
        List<UserScore> userScoreList = userSubmitMapper.selectUserScoreList(examIdSet);
        //根据不同竞赛id进行分组
        Map<Long, List<UserScore>> userScoreMap = userScoreList.stream()
                .collect(Collectors.groupingBy(UserScore::getExamId));
        //创建消息
        createMessage(examList, userScoreMap);
    }

    //消息创建
    private void createMessage(List<Exam> examList, Map<Long, List<UserScore>> userScoreMap) {
        //消息内容列表
        List<MessageText> messageTextList = new ArrayList<>();
        //消息对象列表
        List<Message> messageList = new ArrayList<>();

        //根据竞赛id进行分组，获取每个竞赛对应的用户提交信息
        for (Exam exam : examList) {
            Long examId = exam.getExamId();
            //根据竞赛分组获取不同竞赛的排名信息
            List<UserScore> userScoreList = userScoreMap.get(examId);
            int totalUser = userScoreList.size();
            int examRank = 1;
            //根据用户
            for (UserScore userScore : userScoreList) {
                String msgTitle =  exam.getTitle() + "——排名情况";
                String msgContent = "您所参与的竞赛：" + exam.getTitle()
                        + "，本次参与竞赛一共" + totalUser + "人， 您排名第"  + examRank + "名！";
                userScore.setExamRank(examRank);

                //消息内容
                MessageText messageText = new MessageText();
                messageText.setMessageTitle(msgTitle);
                messageText.setMessageContent(msgContent);
                messageText.setCreateBy(Constants.SYSTEM_USER_ID);
                messageTextList.add(messageText);

                //消息对象
                Message message = new Message();
                message.setSendId(Constants.SYSTEM_USER_ID);
                message.setCreateBy(Constants.SYSTEM_USER_ID);
                message.setRecId(userScore.getUserId());
                messageList.add(message);

                examRank++;
            }
            //更新用户成绩和排名
            userExamMapper.updateUserScoreAndRank(userScoreList);
            //把结果保存到redis中
            redisService.rightPushAll(getExamRankListKey(examId), userScoreList);
        }

        //保存消息内容到数据库中
        messageTextService.batchInsert(messageTextList);

        //根据消息id进行分组存放，并建立消息对象和消息内容的联系
        Map<String, MessageTextVO> messageTextVOMap = new HashMap<>();
        for (int i = 0; i < messageTextList.size(); i++) {
            //赋值
            MessageText messageText = messageTextList.get(i);
            MessageTextVO messageTextVO = new MessageTextVO();
            BeanUtil.copyProperties(messageText, messageTextVO);
            //存放
            String msgDetailKey = getMsgDetailKey(messageText.getTextId());
            messageTextVOMap.put(msgDetailKey, messageTextVO);

            //建立消息对象和消息内容的联系
            Message message = messageList.get(i);
            message.setTextId(messageText.getTextId());
        }
        //保存消息对象到数据库中
        messageService.batchInsert(messageList);

        //存放到redis中
        //根据接收人进行分组存放
        Map<Long, List<Message>> userMsgMap = messageList.stream().collect(Collectors.groupingBy(Message::getRecId));
        //迭代器构造
        Iterator<Map.Entry<Long, List<Message>>> iterator = userMsgMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, List<Message>> entry = iterator.next();
            Long recId = entry.getKey();
            String userMsgListKey = getUserMsgListKey(recId);
            //缓存中存放textid列表
            List<Long> userMsgTextIdList = entry.getValue().stream().map(Message::getTextId).toList();
            redisService.rightPushAll(userMsgListKey, userMsgTextIdList);
        }

        redisService.multiSet(messageTextVOMap);
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
        redisService.rightPushAll(examListKey, examIdList); //尾插
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

