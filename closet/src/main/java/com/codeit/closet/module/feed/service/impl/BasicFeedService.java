package com.codeit.closet.module.feed.service.impl;

import com.codeit.closet.module.feed.dto.FeedCreateRequest;
import com.codeit.closet.module.feed.dto.FeedDTO;
import com.codeit.closet.module.feed.dto.FeedDTOCursorResponse;
import com.codeit.closet.module.feed.dto.FeedUpdateRequest;
import com.codeit.closet.module.feed.mapper.FeedMapper;
import com.codeit.closet.module.feed.repository.FeedRepository;
import com.codeit.closet.module.feed.service.FeedService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BasicFeedService implements FeedService {

  private final FeedRepository feedRepository;
  private final FeedMapper feedMapper;

  @Override
  public FeedDTOCursorResponse findFeeds(String cursor, UUID idAfter, Integer limit, String sortBy,
      String sortDirection, String keywordLike, UUID authorIdEqual) {
    return null;
  }

  @Override
  public FeedDTO createFeed(FeedCreateRequest request) {
    return null;
  }

  @Override
  public FeedDTO updateFeed(UUID feedId, FeedUpdateRequest request) {
    return null;
  }

  @Override
  public void deleteFeed(UUID feedId) {

  }
}
