package com.codeit.closet.common.util.init;

import com.codeit.closet.module.elastic.service.FeedElasticService;
import com.codeit.closet.module.feed.repository.FeedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FeedInitService {

  private final FeedRepository feedRepository;
  private final FeedElasticService feedElasticService;

  @Transactional(readOnly = true)
  public void reindex() {
    feedRepository.findAll()
        .forEach(feedElasticService::index);
  }
}
