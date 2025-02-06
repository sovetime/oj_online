package org.example.ojfriend.domain.exam;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import org.example.common.core.domain.BaseEnity;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("tb_exam")
public class Exam extends BaseEnity {

    @TableId(value = "EXAM_ID", type = IdType.ASSIGN_ID)
    private Long examId;

    private String title;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer status;
}
