package org.example.ojjob.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.ojjob.domain.user.UserExam;
import org.example.ojjob.domain.user.UserScore;


import java.util.List;


public interface UserExamMapper extends BaseMapper<UserExam> {

    void updateUserScoreAndRank(List<UserScore> userScoreList);
}
