package org.example.ojjudge.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.api.domain.UserExeResult;
import org.example.api.domain.dto.JudgeSubmitDTO;
import org.example.api.domain.vo.UserQuestionResultVO;
import org.example.common.core.constants.Constants;
import org.example.common.core.constants.JudgeConstants;
import org.example.common.core.enums.CodeRunStatus;
import org.example.ojjudge.domain.SandBoxExecuteResult;
import org.example.ojjudge.domain.UserSubmit;
import org.example.ojjudge.mapper.UserSubmitMapper;
import org.example.ojjudge.service.IJudgeService;
import org.example.ojjudge.service.ISandboxPoolService;
import org.example.ojjudge.service.ISandboxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * Date: 2025-01-29
 * Time: 22:49
 */

@Service
@Slf4j
public class IJudgeServiceImpl implements IJudgeService {

    @Autowired
    private UserSubmitMapper userSubmitMapper;
    @Autowired
    private ISandboxPoolService sandboxPoolService;

    @Override
    public UserQuestionResultVO doJudgeJavaCode(JudgeSubmitDTO judgeSubmitDTO) {
        log.info("---- 判题逻辑开始 -------");
        //执行代码
        SandBoxExecuteResult sandBoxExecuteResult = sandboxPoolService.exeJavaCode
                (judgeSubmitDTO.getUserId(), judgeSubmitDTO.getUserCode(), judgeSubmitDTO.getInputList());

        UserQuestionResultVO userQuestionResultVO = new UserQuestionResultVO();
        if (sandBoxExecuteResult != null && CodeRunStatus.SUCCEED.equals(sandBoxExecuteResult.getRunStatus())) {
            //比对直接结果  时间限制  空间限制的比对
            userQuestionResultVO = doJudge(judgeSubmitDTO, sandBoxExecuteResult, userQuestionResultVO);
        } else {
            userQuestionResultVO.setPass(Constants.FALSE);
            if (sandBoxExecuteResult != null) {
                userQuestionResultVO.setExeMessage(sandBoxExecuteResult.getExeMessage());
            } else {
                userQuestionResultVO.setExeMessage(CodeRunStatus.UNKNOWN_FAILED.getMsg());
            }
            userQuestionResultVO.setScore(JudgeConstants.ERROR_SCORE);
        }

        //保存用户提交记录
        saveUserSubmit(judgeSubmitDTO, userQuestionResultVO);
        log.info("判题逻辑结束，判题结果为： {} ", userQuestionResultVO);
        return userQuestionResultVO;
    }

    //比对结果
    private UserQuestionResultVO doJudge(JudgeSubmitDTO judgeSubmitDTO,
                                         SandBoxExecuteResult sandBoxExecuteResult,
                                         UserQuestionResultVO userQuestionResultVO) {
        //实际输出
        List<String> exeOutputList = sandBoxExecuteResult.getOutputList();
        //期望输出
        List<String> outputList = judgeSubmitDTO.getOutputList();
        //输出数量不一致的比较
        if (outputList.size() != exeOutputList.size()) {
            userQuestionResultVO.setScore(JudgeConstants.ERROR_SCORE);
            userQuestionResultVO.setPass(Constants.FALSE);
            userQuestionResultVO.setExeMessage(CodeRunStatus.NOT_ALL_PASSED.getMsg());
            return userQuestionResultVO;
        }

        List<UserExeResult> userExeResultList = new ArrayList<>();
        //输入输出的比较
        boolean passed = resultCompare(judgeSubmitDTO, exeOutputList, outputList, userExeResultList);
        //组装其他信息并返回
        return assembleUserQuestionResultVO(judgeSubmitDTO, sandBoxExecuteResult, userQuestionResultVO, userExeResultList, passed);
    }

    //组装其他信息
    private UserQuestionResultVO assembleUserQuestionResultVO(JudgeSubmitDTO judgeSubmitDTO,
                                                              SandBoxExecuteResult sandBoxExecuteResult,
                                                              UserQuestionResultVO userQuestionResultVO,
                                                              List<UserExeResult> userExeResultList, boolean passed) {
        //用户实际输出
        userQuestionResultVO.setUserExeResultList(userExeResultList);
        if (!passed) {
            userQuestionResultVO.setPass(Constants.FALSE);
            userQuestionResultVO.setScore(JudgeConstants.ERROR_SCORE);
            userQuestionResultVO.setExeMessage(CodeRunStatus.NOT_ALL_PASSED.getMsg());
            return userQuestionResultVO;
        }
        //执行结果正确，但是超出空间限制
        if (sandBoxExecuteResult.getUseMemory() > judgeSubmitDTO.getSpaceLimit()) {
            userQuestionResultVO.setPass(Constants.FALSE);
            userQuestionResultVO.setScore(JudgeConstants.ERROR_SCORE);
            userQuestionResultVO.setExeMessage(CodeRunStatus.OUT_OF_MEMORY.getMsg());
            return userQuestionResultVO;
        }
        //执行结果正确，但是超出时间限制
        if (sandBoxExecuteResult.getUseTime() > judgeSubmitDTO.getTimeLimit()) {
            userQuestionResultVO.setPass(Constants.FALSE);
            userQuestionResultVO.setScore(JudgeConstants.ERROR_SCORE);
            userQuestionResultVO.setExeMessage(CodeRunStatus.OUT_OF_TIME.getMsg());
            return userQuestionResultVO;
        }
        userQuestionResultVO.setPass(Constants.TRUE);
        //分数计算，难度不同的题目获得的分数是不同的
        int score = judgeSubmitDTO.getDifficulty() * JudgeConstants.DEFAULT_SCORE;
        userQuestionResultVO.setScore(score);
        return userQuestionResultVO;
    }

    //输入输出的比较
    private boolean resultCompare(JudgeSubmitDTO judgeSubmitDTO, List<String> exeOutputList,
                                  List<String> outputList, List<UserExeResult> userExeResultList) {
        boolean passed = true;
        //比对和期望输出的区别
        for (int index = 0; index < outputList.size(); index++) {
            String input = judgeSubmitDTO.getInputList().get(index);
            String output = outputList.get(index);
            String exeOutPut = exeOutputList.get(index);

            UserExeResult userExeResult = new UserExeResult();
            userExeResult.setInput(input);
            userExeResult.setOutput(output);
            userExeResult.setExeOutput(exeOutPut);
            userExeResultList.add(userExeResult);
            if (!output.equals(exeOutPut)) {
                passed = false;
                log.info("输入：{}， 期望输出：{}， 实际输出：{} ", input, output, exeOutputList);
            }
        }
        return passed;
    }

    //保存用户提交记录
    private void saveUserSubmit(JudgeSubmitDTO judgeSubmitDTO, UserQuestionResultVO userQuestionResultVO) {
        UserSubmit userSubmit = new UserSubmit();
        BeanUtil.copyProperties(userQuestionResultVO, userSubmit);

        userSubmit.setUserId(judgeSubmitDTO.getUserId());
        userSubmit.setQuestionId(judgeSubmitDTO.getQuestionId());
        userSubmit.setExamId(judgeSubmitDTO.getExamId());
        userSubmit.setProgramType(judgeSubmitDTO.getProgramType());
        userSubmit.setUserCode(judgeSubmitDTO.getUserCode());
        userSubmit.setCaseJudgeRes(JSON.toJSONString(userQuestionResultVO.getUserExeResultList()));
        userSubmit.setCreateBy(judgeSubmitDTO.getUserId());

        //这里用户返回的数据是用户最后一次提交的数据，在后续代码扩展中可以考虑更改，给用户保存历史提交记录
        //竞赛、答题两个答题记录分开存储
        userSubmitMapper.delete(new LambdaQueryWrapper<UserSubmit>()
                .eq(UserSubmit::getUserId, judgeSubmitDTO.getUserId())
                .eq(UserSubmit::getQuestionId, judgeSubmitDTO.getQuestionId())
                .isNull(judgeSubmitDTO.getExamId() == null, UserSubmit::getExamId)
                .eq(judgeSubmitDTO.getExamId() != null, UserSubmit::getExamId, judgeSubmitDTO.getExamId()));
        userSubmitMapper.insert(userSubmit);
    }
}













