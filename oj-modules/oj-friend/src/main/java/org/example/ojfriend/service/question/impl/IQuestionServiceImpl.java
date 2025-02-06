package org.example.ojfriend.service.question.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.example.common.core.domain.TableDataInfo;
import org.example.common.core.enums.ResultCode;
import org.example.ojfriend.domain.question.Question;
import org.example.ojfriend.domain.question.dto.QuestionQueryDTO;
import org.example.ojfriend.domain.question.es.QuestionES;
import org.example.ojfriend.domain.question.vo.QuestionDetailVO;
import org.example.ojfriend.domain.question.vo.QuestionVO;
import org.example.ojfriend.elasticsearch.QuestionRepository;
import org.example.ojfriend.manager.QuestionCacheManager;
import org.example.ojfriend.mapper.question.QuestionMapper;
import org.example.ojfriend.mapper.user.UserExamMapper;
import org.example.ojfriend.service.question.IQuestionService;
import org.example.security.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * Date: 2025-01-25
 * Time: 09:44
 */
@Service
@Slf4j
public class IQuestionServiceImpl implements IQuestionService {

    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private QuestionMapper questionMapper;
    @Autowired
    private QuestionCacheManager questionCacheManager;


    @Override
     public TableDataInfo list(QuestionQueryDTO questionQueryDTO) {
        //获取ES题库数据
        long count = questionRepository.count();
        if (count <= 0) {
            refreshQuestion();
        }

        //分页排序
        //Sort.by() 是 Spring Data 中用于创建排序对象的方法
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        Pageable pageable = PageRequest.of(questionQueryDTO.getPageNum() - 1, questionQueryDTO.getPageSize(), sort);

        //获取查询条件
        Integer difficulty = questionQueryDTO.getDifficulty();
        String keyword = questionQueryDTO.getKeyword();

        //查询数据
        Page<QuestionES> questionESPage;
        if (difficulty == null && StrUtil.isEmpty(keyword)) {
            //查询所有数据
            questionESPage = questionRepository.findAll(pageable);
        } else if (StrUtil.isEmpty(keyword)) {
            //查询指定难度数据
            questionESPage = questionRepository.findQuestionByDifficulty(difficulty, pageable);
        } else if (difficulty == null) {
            //查询指定关键字数据
            questionESPage = questionRepository.findByTitleOrContent(keyword, keyword, pageable);
        } else {
            //查询指定难度和关键字数据
            questionESPage = questionRepository.findByTitleOrContentAndDifficulty(keyword, keyword, difficulty, pageable);
        }

        //没有相对应的查询条件，返回null列表
        long total = questionESPage.getTotalElements();
        if (total <= 0) {
            return TableDataInfo.empty();
        }

        List<QuestionES> questionESList = questionESPage.getContent();//获取ES题库数据
        List<QuestionVO> questionVOList = BeanUtil.copyToList(questionESList, QuestionVO.class);
        return TableDataInfo.success(questionVOList, total);
    }

    @Override
    public List<QuestionVO> hotList() {
        Long total = questionCacheManager.getHostListSize();

        return List.of();
    }

    @Override
    public QuestionDetailVO detail(Long questionId) {
        //先从ES题库获取数据
        QuestionES questionES = questionRepository.findById(questionId).orElse(null);
        QuestionDetailVO questionDetailVO = new QuestionDetailVO();
        if (questionES != null) {
            BeanUtil.copyProperties(questionES, questionDetailVO);
            return questionDetailVO;
        }

        //ES题库中没有直接从数据库里查询
        Question question = questionMapper.selectById(questionId);
        if (question == null) {
            return null;
        }
        //刷新ES题库数据
        refreshQuestion();
        BeanUtil.copyProperties(question, questionDetailVO);
        return questionDetailVO;
    }

    @Override
    public String preQuestion(Long questionId) {
        Long listSize = questionCacheManager.getListSize();
        if(listSize==null||listSize<=0){
            questionCacheManager.refreshCache();//刷新ES题库数据
        }
        return questionCacheManager.preQuestion(questionId).toString();
    }

    @Override
    public String nextQuestion(Long questionId) {
        Long listSize = questionCacheManager.getListSize();
        if (listSize == null || listSize <= 0) {
            questionCacheManager.refreshCache();
        }
        return questionCacheManager.nextQuestion(questionId).toString();
    }

    //刷新ES题库数据
    private void refreshQuestion() {
        List<Question> questionList = questionMapper.selectList(new LambdaQueryWrapper<Question>());
        if (CollectionUtil.isEmpty(questionList)) {
            return;
        }
        //转换为ES实体
        List<QuestionES> questionESList = BeanUtil.copyToList(questionList, QuestionES.class);
        //保存到ES
        questionRepository.saveAll(questionESList);
    }
}
