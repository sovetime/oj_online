package org.example.ojfriend.elasticsearch;

import org.example.ojfriend.domain.question.es.QuestionES;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
//这是一个针对 QuestionES实体的ElasticSearch仓库接口，主键类型为 Long
public interface QuestionRepository extends ElasticsearchRepository<QuestionES, Long> {

    //根据题目难度进行查询，并支持分页查询
    Page<QuestionES> findQuestionByDifficulty(Integer difficulty, Pageable pageable);

    //根据（标题或者内容进行查询）和题目难度进行查询，并支持分页查询
    @Query("{\"bool\": {\"should\": [{ \"match\": { \"title\": \"?0\" } }, { \"match\": { \"content\": \"?1\" } }]," +
            " \"minimum_should_match\": 1, \"must\": [{\"term\": {\"difficulty\": \"?2\"}}]}}")
    Page<QuestionES> findByTitleOrContentAndDifficulty(String keywordTitle, String keywordContent,Integer difficulty,  Pageable pageable);

    //根据标题或者内容进行查询，并支持分页查询
    @Query("{\"bool\": {\"should\": [{ \"match\": { \"title\": \"?0\" } }, { \"match\": { \"content\": \"?1\" } }], " +
            "\"minimum_should_match\": 1}}")
    Page<QuestionES> findByTitleOrContent(String keywordTitle, String keywordContent, Pageable pageable);

}
