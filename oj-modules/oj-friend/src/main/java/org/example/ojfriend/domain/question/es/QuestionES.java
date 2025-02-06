package org.example.ojfriend.domain.question.es;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@Getter
@Setter
@Document(indexName = "idx_question")//索引名
public class QuestionES {

    @Id //主键
    @Field(type = FieldType.Long) //指定 Elasticsearch 中字段的类型
    private Long questionId;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_max_word")
    private String title;

    @Field(type = FieldType.Byte)
    private Integer difficulty;

    @Field(type = FieldType.Long)
    private Long timeLimit;

    @Field(type = FieldType.Long)
    private Long spaceLimit;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_max_word")
    //analyzer：指定索引使用的分词器，searchAnalyzer：搜索时使用的分词器
    private String content;

    @Field(type = FieldType.Text)
    private String questionCase; //题目测试用例

    @Field(type = FieldType.Text)
    private String mainFuc;

    @Field(type = FieldType.Text)
    private String defaultCode;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)//时间格式
    private LocalDateTime createTime;

}
