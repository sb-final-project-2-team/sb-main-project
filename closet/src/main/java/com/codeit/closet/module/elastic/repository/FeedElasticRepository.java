package com.codeit.closet.module.elastic.repository;

import com.codeit.closet.module.elastic.document.FeedDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface FeedElasticRepository extends ElasticsearchRepository<FeedDocument, String> {

}
