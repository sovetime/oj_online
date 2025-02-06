package org.example.ojsystem.domain.question.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


//返回前端的数据
@Getter
@Setter
//当序列化对象为JSON格式时，忽略所有值为null的属性
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuestionVO {

    //QuestionVO对象序列化为JSON时，questionId将通过其toString()方法进行序列化
    //可以防止长整形数值在js环境下精度丢失
    @JsonSerialize(using = ToStringSerializer.class)
    private Long questionId;

    private String title;

    private Integer difficulty;

    private String createName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
