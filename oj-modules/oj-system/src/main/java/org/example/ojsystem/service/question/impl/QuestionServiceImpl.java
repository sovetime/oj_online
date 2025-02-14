package org.example.ojsystem.service.question.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import org.example.common.core.enums.ResultCode;
import org.example.ojsystem.domain.question.Question;
import org.example.ojsystem.domain.question.dto.QuestionAddDTO;
import org.example.ojsystem.domain.question.dto.QuestionEditDTO;
import org.example.ojsystem.domain.question.dto.QuestionQueryDTO;
import org.example.ojsystem.domain.question.es.QuestionES;
import org.example.ojsystem.domain.question.vo.QuestionDetailVO;
import org.example.ojsystem.domain.question.vo.QuestionVO;
import org.example.ojsystem.elasticsearch.QuestionRepository;
import org.example.ojsystem.manager.QuestionCacheManager;
import org.example.ojsystem.mapper.question.QuestionMapper;
import org.example.ojsystem.service.question.IQuestionService;
import org.example.security.exception.ServiceException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * Date: 2025-01-14
 * Time: 21:47
 */

@Service
public class QuestionServiceImpl implements IQuestionService {

    @Autowired
    private QuestionMapper questionMapper;
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private QuestionCacheManager questionCacheManager;

    @Override
    public List<QuestionVO> list(QuestionQueryDTO questionQueryDTO) {
        //给PageHelper 提供每页的数据和页数
        PageHelper.startPage(questionQueryDTO.getPageNum(), questionQueryDTO.getPageSize());
        //获取到符合条件的数据
        List<QuestionVO> questionVOList=questionMapper.selectQuestionList(questionQueryDTO);
        return questionVOList;
    }

    @Override
    public boolean add(QuestionAddDTO questionAddDTO) {
        //查询数据库中是否存在相同标题的文章，有相同标题的文章抛出异常
        List<Question> questionList=questionMapper.selectList(new LambdaQueryWrapper<Question>()
                .eq(Question::getTitle,questionAddDTO.getTitle()));
        if(CollectionUtil.isEmpty(questionList)){
            throw new ServiceException(ResultCode.FAILED_ALREADY_EXISTS);
        }

        //新增题目
        Question question=new Question();
        BeanUtils.copyProperties(questionAddDTO,question);
        int insert = questionMapper.insert(question);
        if(insert<=0){
            return false;
        }

        //保存到es中
        QuestionES questionES=new QuestionES();
        BeanUtil.copyProperties(question, questionES);
        questionRepository.save(questionES);
        //添加缓存
        questionCacheManager.addCache(question.getQuestionId());
        return true;
    }


    @Override
    public QuestionDetailVO detail(Long questionId) {
        Question question = questionMapper.selectById(questionId);
        if(question==null){
            throw new ServiceException(ResultCode.FAILED_NOT_EXISTS);
        }
        QuestionDetailVO questionDetailVO=new QuestionDetailVO();
        BeanUtils.copyProperties(question,questionDetailVO);
        return questionDetailVO;
    }

    @Override
    public int edit(QuestionEditDTO questionEditDTO) {
        //需要根据题目id查询出相应的题目才能进行数据修改
        Question question=questionMapper.selectById(questionEditDTO.getQuestionId());
        if(question==null){
            throw new ServiceException(ResultCode.FAILED_NOT_EXISTS);
        }
        BeanUtils.copyProperties(questionEditDTO, question);

        //对es存储的题库进行更新
        QuestionES questionES = new QuestionES();
        BeanUtil.copyProperties(question, questionES);
        questionRepository.save(questionES);
        return questionMapper.updateById(question);
    }

    @Override
    public int delete(Long questionId) {
        Question question = questionMapper.selectById(questionId);
        if (question == null) {
            throw new ServiceException(ResultCode.FAILED_NOT_EXISTS);
        }
        questionRepository.deleteById(questionId);
        questionCacheManager.deleteCache(questionId);
        return questionMapper.deleteById(questionId);
    }

}
