package com.codeit.closet.module.feed.service;

import com.codeit.closet.module.feed.dto.FeedCreateRequest;
import com.codeit.closet.module.feed.dto.FeedDTO;
import com.codeit.closet.module.feed.dto.FeedDTOCursorResponse;
import com.codeit.closet.module.feed.dto.FeedUpdateRequest;
import com.codeit.closet.module.weather.entity.PrecipitationType;
import com.codeit.closet.module.weather.entity.SkyStatus;
import java.util.UUID;

public interface FeedService {

  FeedDTO createFeed(FeedCreateRequest request);

  FeedDTOCursorResponse findFeeds(String cursor,
      UUID idAfter,
      Integer limit,
      String sortBy,
      String sortDirection,
      String keywordLike,
      SkyStatus skyStatusEqual,
      PrecipitationType precipitationTypeEqual,
      UUID authorIdEqual);

  FeedDTO updateFeed(UUID feedId, FeedUpdateRequest request);

  void deleteFeed(UUID feedId);
}
