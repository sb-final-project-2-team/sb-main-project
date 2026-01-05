package com.codeit.closet.module.feed.service;

import com.codeit.closet.module.feed.dto.FeedCreateRequest;
import com.codeit.closet.module.feed.dto.FeedDTO;
import com.codeit.closet.module.feed.dto.FeedDTOCursorResponse;
import com.codeit.closet.module.feed.dto.FeedUpdateRequest;
import java.util.UUID;

public interface FeedService {

  FeedDTOCursorResponse findFeeds(String cursor,
      UUID idAfter,
      Integer limit,
      String sortBy,
      String sortDirection,
      String keywordLike,
//      SkyStatus skyStatusEqual,
//      PrecipitationType precipitationTypeEqual,
      UUID authorIdEqual);

  FeedDTO createFeed(FeedCreateRequest request);

  FeedDTO updateFeed(UUID feedId, FeedUpdateRequest request);

  void deleteFeed(UUID feedId);
}
