package org.example.ojfriend.service.user.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.example.common.core.constants.Constants;
import org.example.common.core.domain.TableDataInfo;
import org.example.common.core.enums.ExamListType;
import org.example.common.core.enums.ResultCode;
import org.example.common.core.utils.ThreadLocalUtil;
import org.example.ojfriend.domain.exam.Exam;
import org.example.ojfriend.domain.exam.dto.ExamQueryDTO;
import org.example.ojfriend.domain.exam.vo.ExamVO;
import org.example.ojfriend.domain.user.UserExam;
import org.example.ojfriend.domain.user.vo.UserVO;
import org.example.ojfriend.manager.ExamCacheManager;
import org.example.ojfriend.manager.UserCacheManager;
import org.example.ojfriend.mapper.exam.ExamMapper;
import org.example.ojfriend.mapper.user.UserExamMapper;
import org.example.ojfriend.service.user.IUserExamService;
import org.example.security.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * Date: 2025-01-21
 * Time: 22:14
 */

@Service
public class IUserExamServiceImpl implements IUserExamService {

    @Autowired
    private ExamMapper examMapper;
    @Autowired
    private UserExamMapper userExamMapper;
    @Autowired
    private ExamCacheManager examCacheManager;
    @Autowired
    private UserCacheManager userCacheManager;

    @Override
    public int enter(String token, Long examId) {
        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            throw new ServiceException(ResultCode.FAILED_NOT_EXISTS);
        }
        //竞赛开始不能报名参赛
        if(exam.getStartTime().isBefore(LocalDateTime.now())) {
            throw new ServiceException(ResultCode.EXAM_STARTED);
        }
        //获取userid并进行查询操作
        Long userId = ThreadLocalUtil.get(Constants.USER_ID, Long.class);
        UserExam userExam = userExamMapper.selectOne(new LambdaQueryWrapper<UserExam>()
                .eq(UserExam::getExamId, examId)
                .eq(UserExam::getUserId, userId));
        if (userExam != null) {
            throw new ServiceException(ResultCode.USER_EXAM_HAS_ENTER);
        }

        examCacheManager.addUserExamCache(userId, examId);
        userExam = new UserExam();
        userExam.setExamId(examId);
        userExam.setUserId(userId);
        return userExamMapper.insert(userExam);
    }

    @Override
    public TableDataInfo list(ExamQueryDTO examQueryDTO) {
        Long userId=ThreadLocalUtil.get(Constants.USER_ID,Long.class);
        //查找用户参加的竞赛
        examQueryDTO.setType(ExamListType.USER_EXAM_LIST.getValue());
        Long total = examCacheManager.getListSize(ExamListType.USER_EXAM_LIST.getValue(), userId);

        List<ExamVO> examVOList;
        if (total == null || total <= 0) {
            //从数据库中查询我的竞赛列表
            PageHelper.startPage(examQueryDTO.getPageNum(), examQueryDTO.getPageSize());
            examVOList = userExamMapper.selectUserExamList(userId);
            examCacheManager.refreshCache(ExamListType.USER_EXAM_LIST.getValue(), userId);
            total = new PageInfo<>(examVOList).getTotal();
        } else {
            examVOList = examCacheManager.getExamVOList(examQueryDTO, userId);
            total = examCacheManager.getListSize(examQueryDTO.getType(), userId);
        }
        if (CollectionUtil.isEmpty(examVOList)) {
            return TableDataInfo.empty();
        }
        return TableDataInfo.success(examVOList, total);
    }
}


























