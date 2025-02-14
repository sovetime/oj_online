package org.example.ojfriend.manager;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import org.example.common.core.constants.CacheConstants;
import org.example.common.core.constants.Constants;
import org.example.common.core.enums.ExamListType;
import org.example.common.core.enums.ResultCode;
import org.example.ojfriend.domain.exam.Exam;
import org.example.ojfriend.domain.exam.ExamQuestion;
import org.example.ojfriend.domain.exam.dto.ExamQueryDTO;
import org.example.ojfriend.domain.exam.dto.ExamRankDTO;
import org.example.ojfriend.domain.exam.vo.ExamRankVO;
import org.example.ojfriend.domain.exam.vo.ExamVO;
import org.example.ojfriend.domain.user.UserExam;
import org.example.ojfriend.mapper.exam.ExamMapper;
import org.example.ojfriend.mapper.exam.ExamQuestionMapper;
import org.example.ojfriend.mapper.user.UserExamMapper;
import org.example.redis.service.RedisService;
import org.example.security.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class ExamCacheManager {

    @Autowired
    private ExamMapper examMapper;
    @Autowired
    private ExamQuestionMapper examQuestionMapper;
    @Autowired
    private UserExamMapper userExamMapper;
    @Autowired
    private RedisService redisService;

    //根据传入竞赛列表所需要的状态获取缓存列表大小
    public Long getListSize(Integer examListType, Long userId) {
        String examListKey = getExamListKey(examListType, userId);
        return redisService.getListSize(examListKey);
    }

    //获取竞赛题目列表的大小
    public Long getExamQuestionListSize(Long examId) {
        String examQuestionListKey = getExamQuestionListKey(examId);
        return redisService.getListSize(examQuestionListKey);
    }

    //获取竞赛排行榜列表的大小
    public Long getRankListSize(Long examId) {
        return redisService.getListSize(getExamRankListKey(examId));
    }

    //获取竞赛列表
    public List<ExamVO> getExamVOList(ExamQueryDTO examQueryDTO, Long userId) {
        //每一页的起始索引和结束索引
        int start = (examQueryDTO.getPageNum() - 1) * examQueryDTO.getPageSize();
        int end = start + examQueryDTO.getPageSize() - 1; //下标需要 -1

        //获取竞赛列表的key
        String examListKey = getExamListKey(examQueryDTO.getType(), userId);
        //获取竞赛的id
        List<Long> examIdList = redisService.getCacheListByRange(examListKey, start, end, Long.class);
        //根据每一页对应的竞赛id拼接数据
        List<ExamVO> examVOList = assembleExamVOList(examIdList);
        //如果redis中没有数据，则从数据库中获取数据
        if (CollectionUtil.isEmpty(examVOList)) {
            //redis中的数据有问题，从数据库中获取数据
            examVOList = getExamListByDB(examQueryDTO, userId);
            refreshCache(examQueryDTO.getType(), userId);
        }
        return examVOList;
    }

    //获取竞赛排名列表
    public List<ExamRankVO> getExamRankList(ExamRankDTO examRankDTO) {
        int start = (examRankDTO.getPageNum() - 1) * examRankDTO.getPageSize();
        int end = start + examRankDTO.getPageSize() - 1;
        return redisService.getCacheListByRange(getExamRankListKey(examRankDTO.getExamId()), start, end, ExamRankVO.class);
    }

    //获取用户参加的竞赛列表
    public List<Long> getAllUserExamList(Long userId) {
        String examListKey = CacheConstants.USER_EXAM_LIST + userId;
        //start=0是从列表第一个元素开始 end=-1代表列表最后一个元素 这里是获取整个列表
        List<Long> userExamIdList = redisService.getCacheListByRange(examListKey, 0, -1, Long.class);
        if (CollectionUtil.isNotEmpty(userExamIdList)) {
            return userExamIdList;
        }

        List<UserExam> userExamList = userExamMapper.selectList(new LambdaQueryWrapper<UserExam>()
                                            .eq(UserExam::getUserId, userId));
        if (CollectionUtil.isEmpty(userExamList)) {
            return null;
        }
        refreshCache(ExamListType.USER_EXAM_LIST.getValue(), userId);//刷新缓存

        return userExamList.stream().map(UserExam::getExamId).collect(Collectors.toList());//转换为Long类型
    }

    public void addUserExamCache(Long userId, Long examId) {
        String userExamListKey = getUserExamListKey(userId);
        redisService.leftPushForList(userExamListKey, examId);
    }

    //获取竞赛的第一个题目
    public Long getFirstQuestion(Long examId) {
        return redisService.indexForList(getExamQuestionListKey(examId), 0, Long.class);
    }

    //获取竞赛的上一个题目
    public Long preQuestion(Long examId, Long questionId) {
        Long index = redisService.indexOfForList(getExamQuestionListKey(examId), questionId);
        if (index == 0) {
            throw new ServiceException(ResultCode.FAILED_FIRST_QUESTION);
        }
        return redisService.indexForList(getExamQuestionListKey(examId), index - 1, Long.class);
    }

    //获取竞赛的下一个题目
    public Long nextQuestion(Long examId, Long questionId) {
        Long index = redisService.indexOfForList(getExamQuestionListKey(examId), questionId);
        long lastIndex = getExamQuestionListSize(examId) - 1;
        if (index == lastIndex) {
            throw new ServiceException(ResultCode.FAILED_LAST_QUESTION);
        }
        return redisService.indexForList(getExamQuestionListKey(examId), index + 1, Long.class);
    }

    //根据类型刷新缓存
    public void refreshCache(Integer examListType, Long userId) {
        List<Exam> examList = new ArrayList<>();
        //根据类型查询竞赛列表
        if (ExamListType.EXAM_UN_FINISH_LIST.getValue().equals(examListType)) {
            //查询未完赛的竞赛列表
            examList = examMapper.selectList(new LambdaQueryWrapper<Exam>()
                    .select(Exam::getExamId, Exam::getTitle, Exam::getStartTime, Exam::getEndTime)
                    .gt(Exam::getEndTime, LocalDateTime.now())//大于当前时间
                    .eq(Exam::getStatus, Constants.TRUE)
                    .orderByDesc(Exam::getCreateTime));

        } else if (ExamListType.EXAM_HISTORY_LIST.getValue().equals(examListType)) {
            //查询历史竞赛
            examList = examMapper.selectList(new LambdaQueryWrapper<Exam>()
                    .select(Exam::getExamId, Exam::getTitle, Exam::getStartTime, Exam::getEndTime)
                    .le(Exam::getEndTime, LocalDateTime.now())
                    .eq(Exam::getStatus, Constants.TRUE)
                    .orderByDesc(Exam::getCreateTime));

        } else if (ExamListType.USER_EXAM_LIST.getValue().equals(examListType)) {
            //查询用户个人竞赛
            List<ExamVO> examVOList = userExamMapper.selectUserExamList(userId);
            examList = BeanUtil.copyToList(examVOList, Exam.class);
        }

        if (CollectionUtil.isEmpty(examList)) {
            return;
        }

        Map<String, Exam> examMap = new HashMap<>();
        List<Long> examIdList = new ArrayList<>();
        for (Exam exam : examList) {
            examMap.put(getDetailKey(exam.getExamId()), exam);
            examIdList.add(exam.getExamId());
        }
        redisService.multiSet(examMap);  //刷新详情缓存
        redisService.deleteObject(getExamListKey(examListType, userId));
        redisService.rightPushAll(getExamListKey(examListType, userId), examIdList);      //刷新列表缓存
    }

    //刷新竞赛题目缓存
    public void refreshExamQuestionCache(Long examId) {
        //examId对应的竞赛题目列表没有数据查询数据库并进行尾插
        List<ExamQuestion> examQuestionList = examQuestionMapper.selectList(new LambdaQueryWrapper<ExamQuestion>()
                .select(ExamQuestion::getQuestionId)
                .eq(ExamQuestion::getExamId, examId)
                .orderByAsc(ExamQuestion::getQuestionOrder));
        if (CollectionUtil.isEmpty(examQuestionList)) {
            return;
        }
        List<Long> examQuestionIdList = examQuestionList.stream().map(ExamQuestion::getQuestionId).toList();
        redisService.rightPushAll(getExamQuestionListKey(examId), examQuestionIdList);

        //节省 redis缓存资源，缓存在第二天零点过期
        long seconds = ChronoUnit.SECONDS.between(LocalDateTime.now(),
                LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0));
        redisService.expire(getExamQuestionListKey(examId), seconds, TimeUnit.SECONDS);
    }


    //刷新竞赛排行榜缓存
    public void refreshExamRankCache(Long examId) {
        List<ExamRankVO> examRankVOList = userExamMapper.selectExamRankList(examId);
        if (CollectionUtil.isEmpty(examRankVOList)) {
            return;
        }
        redisService.rightPushAll(getExamRankListKey(examId), examRankVOList);
    }

    //获取redis当中的竞赛列表
    private List<ExamVO> assembleExamVOList(List<Long> examIdList) {
        if (CollectionUtil.isEmpty(examIdList)) {
            //说明redis当中没数据 从数据库中查数据并且重新刷新缓存
            return null;
        }
        //拼接redis中key存储的examid，并且将拼接好的key存储到一个list中
        List<String> detailKeyList = new ArrayList<>();
        for (Long examId : examIdList) {
            detailKeyList.add(getDetailKey(examId));
        }

        //从redis中批量获取数据
        List<ExamVO> examVOList = redisService.multiGet(detailKeyList, ExamVO.class);
        //去除空值
        CollUtil.removeNull(examVOList);
        if (CollectionUtil.isEmpty(examVOList) || examVOList.size() != examIdList.size()) {
            //说明redis中数据有问题 从数据库中查数据并且重新刷新缓存
            return null;
        }
        return examVOList;
    }


    //根据竞赛状态获取缓存key
    private String getExamListKey(Integer examListType, Long userId) {
        if (ExamListType.EXAM_UN_FINISH_LIST.getValue().equals(examListType)) {
            //竞赛还没有结束的
            return CacheConstants.EXAM_UNFINISHED_LIST;
        } else if (ExamListType.EXAM_HISTORY_LIST.getValue().equals(examListType)) {
            //历史竞赛列表
            return CacheConstants.EXAM_HISTORY_LIST;
        } else {
            //用户个人竞赛列表
            return CacheConstants.USER_EXAM_LIST + userId;
        }
    }

    //从数据库中获取竞赛列表
    private List<ExamVO> getExamListByDB(ExamQueryDTO examQueryDTO, Long userId) {
        PageHelper.startPage(examQueryDTO.getPageNum(), examQueryDTO.getPageSize());
        if (ExamListType.USER_EXAM_LIST.getValue().equals(examQueryDTO.getType())) {
            //查询我的竞赛列表
            return userExamMapper.selectUserExamList(userId);
        } else {
            //查询C端的竞赛列表
            return examMapper.selectExamList(examQueryDTO);
        }
    }

    private String getDetailKey(Long examId) {
        return CacheConstants.EXAM_DETAIL + examId;
    }

    private String getUserExamListKey(Long userId) {
        return CacheConstants.USER_EXAM_LIST + userId;
    }

    private String getExamQuestionListKey(Long examId) {
        return CacheConstants.EXAM_QUESTION_LIST + examId;
    }

    private String getExamRankListKey(Long examId) {
        return CacheConstants.EXAM_RANK_LIST + examId;
    }
}