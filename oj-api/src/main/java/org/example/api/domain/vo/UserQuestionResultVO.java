package org.example.api.domain.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.example.api.domain.UserExeResult;

import java.util.List;

@Getter
@Setter
public class UserQuestionResultVO {

    private Integer pass; // 0-未通过  1-通过

    private String exeMessage; //异常信息

    private List<UserExeResult> userExeResultList;//用户的执行结果

    @JsonIgnore
    private Integer score;
}
