package org.example.ojsystem.elasticsearch;

import org.example.ojsystem.domain.question.es.QuestionES;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends ElasticsearchRepository<QuestionES, Long> {

}
