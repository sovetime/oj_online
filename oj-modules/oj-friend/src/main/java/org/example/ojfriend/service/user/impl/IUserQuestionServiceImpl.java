package org.example.ojfriend.service.user.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.example.api.RemoteJudgeService;
import org.example.api.domain.UserExeResult;
import org.example.api.domain.dto.JudgeSubmitDTO;
import org.example.api.domain.vo.UserQuestionResultVO;
import org.example.common.core.constants.Constants;
import org.example.common.core.domain.R;
import org.example.common.core.enums.ProgramType;
import org.example.common.core.enums.QuestionResType;
import org.example.common.core.enums.ResultCode;
import org.example.common.core.utils.ThreadLocalUtil;
import org.example.ojfriend.domain.question.Question;
import org.example.ojfriend.domain.question.QuestionCase;
import org.example.ojfriend.domain.question.es.QuestionES;
import org.example.ojfriend.domain.user.UserSubmit;
import org.example.ojfriend.domain.user.dto.UserSubmitDTO;
import org.example.ojfriend.elasticsearch.QuestionRepository;
import org.example.ojfriend.mapper.question.QuestionMapper;
import org.example.ojfriend.mapper.user.UserSubmitMapper;
import org.example.ojfriend.rabbit.JudgeProducer;
import org.example.ojfriend.service.user.IUserQuestionService;
import org.example.security.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class IUserQuestionServiceImpl implements IUserQuestionService{

    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private QuestionMapper questionMapper;
    @Autowired
    private UserSubmitMapper userSubmitMapper;
    @Autowired
    private RemoteJudgeService remoteJudgeService;
    @Autowired
    private JudgeProducer judgeProducer;

    @Override
    public R<UserQuestionResultVO> submit(UserSubmitDTO submitDTO) {
        Integer programType = submitDTO.getProgramType();
        if (ProgramType.JAVA.getValue().equals(programType)) {
            //按照java逻辑处理
            JudgeSubmitDTO judgeSubmitDTO = assembleJudgeSubmitDTO(submitDTO);
            return remoteJudgeService.doJudgeJavaCode(judgeSubmitDTO);
        }
        throw new ServiceException(ResultCode.FAILED_NOT_SUPPORT_PROGRAM);
    }

    @Override
    public boolean rabbitSubmit(UserSubmitDTO submitDTO) {
        Integer programType = submitDTO.getProgramType();
        if (ProgramType.JAVA.getValue().equals(programType)) {
            //按照java逻辑处理
            JudgeSubmitDTO judgeSubmitDTO = assembleJudgeSubmitDTO(submitDTO);
            judgeProducer.produceMsg(judgeSubmitDTO);
            return true;
        }
        throw new ServiceException(ResultCode.FAILED_NOT_SUPPORT_PROGRAM);
    }

    @Override
    public UserQuestionResultVO exeResult(Long examId, Long questionId, String currentTime) {
        Long userId = ThreadLocalUtil.get(Constants.USER_ID, Long.class);
        UserSubmit userSubmit = userSubmitMapper.selectCurrentUserSubmit(userId, examId, questionId, currentTime);
        UserQuestionResultVO resultVO = new UserQuestionResultVO();
        if (userSubmit == null) {
            resultVO.setPass(QuestionResType.IN_JUDGE.getValue());
        } else {
            resultVO.setPass(userSubmit.getPass());
            resultVO.setExeMessage(userSubmit.getExeMessage());
            if (StrUtil.isNotEmpty(userSubmit.getCaseJudgeRes())) {
                resultVO.setUserExeResultList(JSON.parseArray(userSubmit.getCaseJudgeRes(), UserExeResult.class));
            }
        }
        return resultVO;
    }


    //组装用户提交的代码 赋值JudgeSubmitDTO属性
    private JudgeSubmitDTO assembleJudgeSubmitDTO(UserSubmitDTO submitDTO) {
        Long questionId = submitDTO.getQuestionId();
        //从es中获取题目信息，orElse(null)表示如果没有查到数据返回null，有数据范围返回对应查询出的数据，findById是Optional类型的
        QuestionES questionES = questionRepository.findById(questionId).orElse(null);

        JudgeSubmitDTO judgeSubmitDTO = new JudgeSubmitDTO();
        if (questionES != null) {
            BeanUtil.copyProperties(questionES, judgeSubmitDTO);
        } else {
            Question question = questionMapper.selectById(questionId);
            BeanUtil.copyProperties(question, judgeSubmitDTO);
            questionES = new QuestionES();
            BeanUtil.copyProperties(question, questionES);
            //保存到es中
            questionRepository.save(questionES);
        }

        judgeSubmitDTO.setUserId(ThreadLocalUtil.get(Constants.USER_ID, Long.class));
        judgeSubmitDTO.setExamId(submitDTO.getExamId());
        judgeSubmitDTO.setProgramType(submitDTO.getProgramType());
        judgeSubmitDTO.setUserCode(codeConnect(submitDTO.getUserCode(), questionES.getMainFuc()));

        //获取题目测试用例
        //将JSON数组字符串转换为指定类型的列表
        List<QuestionCase> questionCaseList = JSONUtil.toList(questionES.getQuestionCase(), QuestionCase.class);
        //获取输入测试样例/输出测试样例
        List<String> inputList = questionCaseList.stream().map(QuestionCase::getInput).toList();
        judgeSubmitDTO.setInputList(inputList);
        List<String> outputList = questionCaseList.stream().map(QuestionCase::getOutput).toList();
        judgeSubmitDTO.setOutputList(outputList);
        return judgeSubmitDTO;
    }

    //拼接用户代码和main函数
    private String codeConnect(String userCode, String mainFunc) {
        String targetCharacter = "}";

        int targetLastIndex = userCode.lastIndexOf(targetCharacter);
        if (targetLastIndex != -1) {
            //substring(left,right)是左闭右开的,substring(index)是截取index后面的字符串(包括index)
            return userCode.substring(0, targetLastIndex) + "\n" + mainFunc + "\n" + userCode.substring(targetLastIndex);
        }
        throw new ServiceException(ResultCode.FAILED);
    }

}
