package org.example.ojfriend.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.ojfriend.domain.exam.vo.ExamRankVO;
import org.example.ojfriend.domain.exam.vo.ExamVO;
import org.example.ojfriend.domain.user.UserExam;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * Date: 2025-01-21
 * Time: 22:15
 */
public interface UserExamMapper extends BaseMapper<UserExam> {
    List<ExamVO> selectUserExamList(Long userId);

    List<ExamRankVO> selectExamRankList(Long examId);

}
