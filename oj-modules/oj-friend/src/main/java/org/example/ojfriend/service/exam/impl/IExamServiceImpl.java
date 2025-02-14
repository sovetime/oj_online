package org.example.ojfriend.service.exam.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.example.common.core.constants.Constants;
import org.example.common.core.domain.TableDataInfo;
import org.example.common.core.utils.ThreadLocalUtil;
import org.example.ojfriend.domain.exam.dto.ExamQueryDTO;
import org.example.ojfriend.domain.exam.dto.ExamRankDTO;
import org.example.ojfriend.domain.exam.vo.ExamRankVO;
import org.example.ojfriend.domain.exam.vo.ExamVO;
import org.example.ojfriend.domain.user.vo.UserVO;
import org.example.ojfriend.manager.ExamCacheManager;
import org.example.ojfriend.manager.UserCacheManager;
import org.example.ojfriend.mapper.exam.ExamMapper;
import org.example.ojfriend.mapper.user.UserExamMapper;
import org.example.ojfriend.service.exam.IExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * Date: 2025-01-21
 * Time: 13:19
 */
@Service
public class IExamServiceImpl implements IExamService {

    @Autowired
    private ExamMapper examMapper;
    @Autowired
    private ExamCacheManager examCacheManager;
    @Autowired
    private UserCacheManager userCacheManager;
    @Autowired
    private UserExamMapper userExamMapper;

    @Override
    public List<ExamVO> list(ExamQueryDTO examQueryDTO) {
        PageHelper.startPage(examQueryDTO.getPageNum(), examQueryDTO.getPageSize());
        return examMapper.selectExamList(examQueryDTO);
    }

    @Override
    public TableDataInfo redisList(ExamQueryDTO examQueryDTO) {
        //根据竞赛的状态获取缓存列表大小
        Long total=examCacheManager.getListSize(examQueryDTO.getType(),null);
        List<ExamVO> examVOList;

        //如果缓存中没有数据，则从数据库中获取数据，并刷新缓存
        if(total==null||total<=0){
            examVOList=list(examQueryDTO);
            examCacheManager.refreshCache(examQueryDTO.getType(),null);
            total=new PageInfo<>(examVOList).getTotal();//获取分页后的总数量
        } else{
            //从缓存中获取数据
            examVOList=examCacheManager.getExamVOList(examQueryDTO,null);
            total=examCacheManager.getListSize(examQueryDTO.getType(),null);
        }
        if(CollectionUtils.isEmpty(examVOList)){
            return TableDataInfo.empty();
        }

        //检测其中是否有当前用户参加的竞赛，并进行状态设置，在游客状态下是返回null的，不会有影响
        assembleExamVOList(examVOList);

        return TableDataInfo.success(examVOList,total);
    }

    @Override
    public TableDataInfo rankList(ExamRankDTO examRankDTO) {
        Long total=examCacheManager.getRankListSize(examRankDTO.getExamId());
        List<ExamRankVO> examRankVOList;

        if (total == null || total <= 0) {
            PageHelper.startPage(examRankDTO.getPageNum(), examRankDTO.getPageSize());
            examRankVOList = userExamMapper.selectExamRankList(examRankDTO.getExamId());
            examCacheManager.refreshExamRankCache(examRankDTO.getExamId());
            total = new PageInfo<>(examRankVOList).getTotal();
        } else {
            //获取竞赛排名列表
            examRankVOList = examCacheManager.getExamRankList(examRankDTO);
        }
        if (CollectionUtil.isEmpty(examRankVOList)) {
            return TableDataInfo.empty();
        }

        //根据用户id获取用户名称，并设置到rankVO中
        assembleExamRankVOList(examRankVOList);
        return TableDataInfo.success(examRankVOList, total);
    }

    @Override
    public String getFirstQuestion(Long examId) {
        checkAndRefresh(examId);
        return examCacheManager.getFirstQuestion(examId).toString();
    }

    @Override
    public String preQuestion(Long examId, Long questionId) {
        checkAndRefresh(examId);
        return examCacheManager.preQuestion(examId, questionId).toString();
    }

    @Override
    public String nextQuestion(Long examId, Long questionId) {
        checkAndRefresh(examId);
        return examCacheManager.nextQuestion(examId, questionId).toString();
    }

    //校验和刷新缓存
    private void checkAndRefresh(Long examId) {
        Long listSize = examCacheManager.getExamQuestionListSize(examId);
        if (listSize == null || listSize <= 0) {
            examCacheManager.refreshExamQuestionCache(examId);
        }
    }

    //检测其中是否有当前用户参加的竞赛，并进行状态设置，在游客状态下是返回null的，不会有影响
    private void assembleExamVOList(List<ExamVO> examVOList) {
        //获取与当前线程绑定的用户id
        Long userId = ThreadLocalUtil.get(Constants.USER_ID, Long.class);
        //获取当前用户参加的竞赛id
        List<Long> userExamIdList = examCacheManager.getAllUserExamList(userId);
        if (CollectionUtil.isEmpty(userExamIdList)) {
            return;
        }
        //遍历竞赛列表，判断当前用户是否参加该竞赛
        for (ExamVO examVO : examVOList) {
            if (userExamIdList.contains(examVO.getExamId())) {
                examVO.setEnter(true);
            }
        }
    }

    //根据用户id获取用户名称，并设置到rankVO中
    private void assembleExamRankVOList(List<ExamRankVO> examRankVOList) {
        if (CollectionUtil.isEmpty(examRankVOList)) {
            return;
        }
        for (ExamRankVO examRankVO : examRankVOList) {
            Long userId = examRankVO.getUserId();
            UserVO user = userCacheManager.getUserById(userId);
            examRankVO.setNickName(user.getNickName());
        }
    }

}


















