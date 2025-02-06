package org.example.ojsystem.mapper.exam;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.ojsystem.domain.exam.Exam;
import org.example.ojsystem.domain.exam.dto.ExamQueryDTO;
import org.example.ojsystem.domain.exam.vo.ExamVO;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * Date: 2025-01-16
 * Time: 15:29
 */

public interface ExamMapper extends BaseMapper<Exam> {

    List<ExamVO> selectExamList(ExamQueryDTO examQueryDTO);

}
