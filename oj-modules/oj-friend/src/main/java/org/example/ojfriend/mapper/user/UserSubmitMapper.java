package org.example.ojfriend.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.ojfriend.domain.user.UserSubmit;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * Date: 2025-01-28
 * Time: 09:33
 */
public interface UserSubmitMapper extends BaseMapper<UserSubmit> {

    UserSubmit selectCurrentUserSubmit(Long userId, Long examId, Long questionId, String currentTime);

}
