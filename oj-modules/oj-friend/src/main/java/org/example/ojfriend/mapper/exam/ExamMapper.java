package org.example.ojfriend.mapper.exam;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.ojfriend.domain.exam.Exam;
import org.example.ojfriend.domain.exam.dto.ExamQueryDTO;
import org.example.ojfriend.domain.exam.vo.ExamVO;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * Date: 2025-01-21
 * Time: 13:18
 */
public interface ExamMapper extends BaseMapper<Exam> {
    List<ExamVO> selectExamList(ExamQueryDTO examQueryDTO);
}
