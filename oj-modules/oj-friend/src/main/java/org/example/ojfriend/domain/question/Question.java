package org.example.ojfriend.domain.question;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import org.example.common.core.domain.BaseEnity;

@TableName("tb_question")
@Getter
@Setter
public class Question extends BaseEnity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long questionId;

    private String title;

    private Integer difficulty;

    private Long timeLimit;

    private Long spaceLimit;

    private String content;

    private String questionCase;

    private String defaultCode;

    private String mainFuc;
}
