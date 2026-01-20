package com.codeit.closet.module.elastic.service;

import com.codeit.closet.module.elastic.document.FeedDocument;
import com.codeit.closet.module.elastic.repository.FeedElasticRepository;
import com.codeit.closet.module.feed.entity.Feed;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FeedElasticService {

  private final FeedElasticRepository feedElasticRepository;

  public void index(Feed feed) {
    feedElasticRepository.save(
        new FeedDocument(
            feed.getId().toString(),
            feed.getContent(),
            feed.getUser().getId().toString(),
            feed.getWeather().getSkyStatus(),
            feed.getWeather().getPrecipitationType(),
            feed.getLikeCount(),
            feed.getCreatedAt()
        )
    );
  }

  public void delete(UUID feedId) {
    feedElasticRepository.deleteById(feedId.toString());
  }
}
