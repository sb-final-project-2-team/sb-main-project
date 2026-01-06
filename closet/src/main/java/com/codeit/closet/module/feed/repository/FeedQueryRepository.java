package com.codeit.closet.module.feed.repository;

import com.codeit.closet.module.feed.dto.FeedDTOCursorResponse;
import com.codeit.closet.module.weather.entity.PrecipitationType;
import com.codeit.closet.module.weather.entity.SkyStatus;
import java.util.UUID;

public interface FeedQueryRepository {

  FeedDTOCursorResponse findFeedsByCursor(
      String cursor, UUID idAfter, Integer limit, String sortBy,
      String sortDirection, String keywordLike, SkyStatus skyStatusEqual,
      PrecipitationType precipitationTypeEqual, UUID authorIdEqual
  );
}
