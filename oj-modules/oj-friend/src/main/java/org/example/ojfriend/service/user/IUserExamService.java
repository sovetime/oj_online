package org.example.ojfriend.service.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.common.core.domain.TableDataInfo;
import org.example.ojfriend.domain.exam.dto.ExamQueryDTO;
import org.example.ojfriend.domain.user.UserExam;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * Date: 2025-01-21
 * Time: 22:10
 */
public interface IUserExamService {

    int enter(String token, Long examId);

    TableDataInfo list(ExamQueryDTO examQueryDTO);
}
