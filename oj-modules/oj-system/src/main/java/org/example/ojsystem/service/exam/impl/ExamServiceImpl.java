package org.example.ojsystem.service.exam.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.common.core.constants.Constants;
import com.github.pagehelper.PageHelper;
import org.example.common.core.enums.ResultCode;
import org.example.ojsystem.domain.exam.Exam;
import org.example.ojsystem.domain.exam.ExamQuestion;
import org.example.ojsystem.domain.exam.dto.ExamAddDTO;
import org.example.ojsystem.domain.exam.dto.ExamEditDTO;
import org.example.ojsystem.domain.exam.dto.ExamQueryDTO;
import org.example.ojsystem.domain.exam.dto.ExamQuestAddDTO;
import org.example.ojsystem.domain.exam.vo.ExamDetailVO;
import org.example.ojsystem.domain.exam.vo.ExamVO;
import org.example.ojsystem.domain.question.Question;
import org.example.ojsystem.domain.question.vo.QuestionVO;
import org.example.ojsystem.manager.ExamCacheManager;
import org.example.ojsystem.mapper.exam.ExamMapper;
import org.example.ojsystem.mapper.exam.ExamQuestionMapper;
import org.example.ojsystem.mapper.question.QuestionMapper;
import org.example.ojsystem.service.exam.IExamService;
import org.example.security.exception.ServiceException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.baomidou.mybatisplus.extension.toolkit.Db.saveBatch;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * Date: 2025-01-16
 * Time: 15:28
 */

@Service
public class ExamServiceImpl implements IExamService {

    @Autowired
    private ExamMapper examMapper;
    @Autowired
    private QuestionMapper questionMapper;
    @Autowired
    private ExamQuestionMapper examQuestionMapper;
    @Autowired
    private ExamCacheManager examCacheManager;

    @Override
    public List<ExamVO> list(ExamQueryDTO examQueryDTO) {
        PageHelper.startPage(examQueryDTO.getPageNum(), examQueryDTO.getPageSize());
        return examMapper.selectExamList(examQueryDTO);
    }

    @Override
    public String add(ExamAddDTO examAddDTO) {
        //判断竞赛标题是否重复，竞赛开始时间不能早于当前时间，竞赛结束时间不能早当前时间，
        checkExamSaveParams(examAddDTO,null);
        Exam exam = new Exam();
        BeanUtils.copyProperties(examAddDTO, exam);

        examMapper.insert(exam);
        return exam.getExamId().toString();
    }

    @Override
    public boolean questionAdd(ExamQuestAddDTO examQuestAddDTO) {
        Exam exam=getExam(examQuestAddDTO.getExamId());
        checkExam(exam);
        if (Constants.TRUE.equals(exam.getStatus())) {
            throw new ServiceException(ResultCode.EXAM_IS_PUBLISH);//竞赛已经发布不能进行编辑、删除操作
        }
        //竞赛状态正确，可以进行题目添加操作
        Set<Long> questionIdSet = examQuestAddDTO.getQuestionIdSet();
        //questionIdSet 为空说明没有需要添加的数据，可以直接返回
        if (CollectionUtil.isEmpty(questionIdSet)) {
            return true;
        }

        //验证题库中的题目是否存在
        List<Question> questionList = questionMapper.selectBatchIds(questionIdSet);
        if (CollectionUtil.isEmpty(questionList) || questionList.size() < questionIdSet.size()) {
            throw new ServiceException(ResultCode.EXAM_QUESTION_NOT_EXISTS);
        }

        //添加题目操作核心在于绑定examid和题目的关系
        return saveExamQuestion(exam, questionIdSet);
    }

    @Override
    public int questionDelete(Long examId, Long questionId) {
        Exam exam = getExam(examId);
        checkExam(exam);
        if (Constants.TRUE.equals(exam.getStatus())) {
            throw new ServiceException(ResultCode.EXAM_IS_PUBLISH);
        }

        //delete from tb_exam_question where examId=#{examId} and questionId=#{questionId}
        return examQuestionMapper.delete(new LambdaQueryWrapper<ExamQuestion>()
                .eq(ExamQuestion::getExamId, examId)
                .eq(ExamQuestion::getQuestionId, questionId));
    }

    @Override
    public ExamDetailVO detail(Long examId) {
        ExamDetailVO examDetailVO = new ExamDetailVO();
        Exam exam = getExam(examId);
        BeanUtils.copyProperties(exam, examDetailVO);

        //通过竞赛id查询出该竞赛和其对应题目的关系
        List<QuestionVO> questionVOList = examQuestionMapper.selectExamQuestionList(examId);
        if (CollectionUtil.isEmpty(questionVOList)) {
            return examDetailVO;
        }
        examDetailVO.setExamQuestionList(questionVOList);
        return examDetailVO;
    }

    @Override
    public int edit(ExamEditDTO examEditDTO) {
        Exam exam = getExam(examEditDTO.getExamId());
        checkExam(exam);
        if (Constants.TRUE.equals(exam.getStatus())) {
            throw new ServiceException(ResultCode.EXAM_IS_PUBLISH);
        }

        //编辑是在查询出数据之后进行的编辑，我们需要确保你修改的竞赛的名称不能和已经有的竞赛一样，因此引入竞赛id经行条件查询
        checkExamSaveParams(examEditDTO, examEditDTO.getExamId());

        exam.setTitle(examEditDTO.getTitle());
        exam.setStartTime(examEditDTO.getStartTime());
        exam.setEndTime(examEditDTO.getEndTime());

        return examMapper.updateById(exam);
    }

    @Override
    public int delete(Long examId) {
        Exam exam = getExam(examId);
        if (Constants.TRUE.equals(exam.getStatus())) {
            throw new ServiceException(ResultCode.EXAM_IS_PUBLISH);
        }
        //确保竞赛没有开始
        checkExam(exam);
        examQuestionMapper.delete(new LambdaQueryWrapper<ExamQuestion>()
                .eq(ExamQuestion::getExamId, examId));
        return examMapper.deleteById(examId);
    }

    @Override
    public int publish(Long examId) {
        Exam exam = getExam(examId);
        //竞赛已经结束不能发布
        if(exam.getEndTime().isBefore(LocalDateTime.now())){
            throw new ServiceException(ResultCode.EXAM_IS_FINISH);
        }

        //需要保证竞赛是有题目的
        Long count=examQuestionMapper.selectCount(new LambdaQueryWrapper<ExamQuestion>()
                .eq(ExamQuestion::getExamId, examId));
        if (count == null || count <= 0) {
            throw new ServiceException(ResultCode.EXAM_NOT_HAS_QUESTION);
        }

        //修改发布状态
        exam.setStatus(Constants.TRUE);
        //要将新发布的竞赛数据存储到redis
        examCacheManager.addCache(exam);

        return examMapper.updateById(exam);
    }

    @Override
    public int cancelPublish(Long examId) {
        Exam exam = getExam(examId);
        //校验竞赛是否开始
        checkExam(exam);
        //竞赛结束后不能撤销发布
        if (exam.getEndTime().isBefore(LocalDateTime.now())) {
            throw new ServiceException(ResultCode.EXAM_IS_FINISH);
        }

        //修改发布状态
        exam.setStatus(Constants.FALSE);
        //删除缓存中的题目信息
        examCacheManager.deleteCache(examId);

        return examMapper.updateById(exam);
    }

    //判断竞赛标题是否重复，竞赛开始时间不能早于当前时间，竞赛结束时间不能早当前时间
    private void checkExamSaveParams(ExamAddDTO examSaveDTO, Long examId) {
        List<Exam> examList = examMapper.selectList(new LambdaQueryWrapper<Exam>()
                .eq(Exam::getTitle, examSaveDTO.getTitle())
                //如果examId!=null,就添加一个查询条件
                .ne(examId != null, Exam::getExamId, examId));

        if (CollectionUtil.isNotEmpty(examList)) {
            throw new ServiceException(ResultCode.FAILED_ALREADY_EXISTS);
        }
        if (examSaveDTO.getStartTime().isBefore(LocalDateTime.now())) {
            throw new ServiceException(ResultCode.EXAM_START_TIME_BEFORE_CURRENT_TIME);
        }
        if (examSaveDTO.getStartTime().isAfter(examSaveDTO.getEndTime())) {
            throw new ServiceException(ResultCode.EXAM_START_TIME_AFTER_END_TIME);
        }
    }

    //绑定好竞赛和题目之间的关系
    private boolean saveExamQuestion(Exam exam, Set<Long> questionIdSet) {
        //num是题目计数器，为每个题目设置顺序
        int num = 1;
        List<ExamQuestion> examQuestionList = new ArrayList<>();
        for (Long questionId : questionIdSet) {
            ExamQuestion examQuestion = new ExamQuestion();
            examQuestion.setExamId(exam.getExamId());
            examQuestion.setQuestionId(questionId);
            examQuestion.setQuestionOrder(num++);
            //将赋值好的对象添加入链表中
            examQuestionList.add(examQuestion);
        }
        //批量保存ExamQuestion对象到数据库中，并返回操作是否成功
        return saveBatch(examQuestionList);
    }

    //根据竞赛id获取竞赛相关的数据
    private Exam getExam(Long examId) {
        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            throw new ServiceException(ResultCode.FAILED_NOT_EXISTS);
        }
        return exam;
    }

    //判断竞赛是否开始，竞赛开始不能进行操作
    private void checkExam(Exam exam) {
        if (exam.getStartTime().isBefore(LocalDateTime.now())) {
            throw new ServiceException(ResultCode.EXAM_STARTED);
        }
    }
}





