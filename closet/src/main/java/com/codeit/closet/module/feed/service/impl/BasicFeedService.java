package com.codeit.closet.module.feed.service.impl;

import com.codeit.closet.module.cloth.entity.Cloth;
import com.codeit.closet.module.cloth.repository.ClothRepository;
import com.codeit.closet.module.elastic.dto.FeedSearchResult;
import com.codeit.closet.module.elastic.service.FeedElasticService;
import com.codeit.closet.module.elastic.service.FeedSearchService;
import com.codeit.closet.module.feed.dto.FeedCreateRequest;
import com.codeit.closet.module.feed.dto.FeedDTO;
import com.codeit.closet.module.feed.dto.FeedDTOCursorResponse;
import com.codeit.closet.module.feed.dto.FeedUpdateRequest;
import com.codeit.closet.module.feed.entity.Feed;
import com.codeit.closet.module.feed.entity.Ootd;
import com.codeit.closet.module.feed.mapper.FeedMapper;
import com.codeit.closet.module.feed.repository.FeedRepository;
import com.codeit.closet.module.feed.service.FeedService;
import com.codeit.closet.module.user.entity.User;
import com.codeit.closet.module.user.repository.UserRepository;
import com.codeit.closet.module.weather.entity.PrecipitationType;
import com.codeit.closet.module.weather.entity.SkyStatus;
import com.codeit.closet.module.weather.entity.WeatherData;
import com.codeit.closet.module.weather.repository.WeatherDataRepository;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BasicFeedService implements FeedService {

  private final UserRepository userRepository;
  private final WeatherDataRepository weatherDataRepository;
  private final ClothRepository clothRepository;
  private final FeedRepository feedRepository;

  private final FeedElasticService feedElasticService;
  private final FeedSearchService feedSearchService;

  private final FeedMapper feedMapper;

  @Override
  @Transactional
  public FeedDTO createFeed(FeedCreateRequest request) {

    User user = userRepository.findById(request.authorId())
        .orElseThrow(() -> new NoSuchElementException("존재하지 않는 회원 정보입니다."));

    WeatherData weatherData = weatherDataRepository.findById(request.weatherId())
        .orElseThrow(() -> new NoSuchElementException("존재하지 않는 날씨 정보 입니다."));

    List<Cloth> clothList =
        clothRepository.findAllById(request.clothesIds());

    Feed feed = Feed.builder()
        .user(user)
        .weather(weatherData)
        .content(request.content())
        .build();

    for (Cloth cloth : clothList) {
      Ootd ootd = Ootd.builder()
          .feed(feed)
          .cloth(cloth)
          .build();

      feed.addOotd(ootd);
    }

    Feed saved = feedRepository.save(feed);
    feedElasticService.index(feed);
    return feedMapper.toFeedDTO(saved);
  }

  @Override
  @Transactional(readOnly = true)
  public FeedDTOCursorResponse findFeeds(String cursor, UUID idAfter, Integer limit, String sortBy,
      String sortDirection, String keywordLike, SkyStatus skyStatusEqual,
      PrecipitationType precipitationTypeEqual, UUID authorIdEqual, UUID principal) {

    if (keywordLike != null || skyStatusEqual != null
        || precipitationTypeEqual != null || authorIdEqual != null) {
      FeedSearchResult result = feedSearchService.searchFeedIds(cursor, idAfter, limit,
          sortBy, sortDirection, keywordLike,
          skyStatusEqual, precipitationTypeEqual, authorIdEqual);

      if (result.feedIds().isEmpty()) {
        return FeedDTOCursorResponse.empty(sortBy, sortDirection);
      }

      List<Feed> feeds = feedRepository.findFeedsByIdIn(result.feedIds());

      Map<UUID, Integer> orderMap = new HashMap<>();
      for (int i = 0; i < result.feedIds().size(); i++) {
        orderMap.put(result.feedIds().get(i), i);
      }

      feeds.sort(Comparator.comparingInt(f -> orderMap.get(f.getId())));
      List<FeedDTO> data = feedMapper.toFeedDTOs(feeds);

      return new FeedDTOCursorResponse(
          data,
          result.nextCursor(),
          result.nextIdAfter(),
          result.hasNext(),
          result.totalCount(),
          sortBy,
          sortDirection
      );
    }
    return feedRepository.findFeedsByCursor(cursor, idAfter, limit, sortBy, sortDirection,
        keywordLike, skyStatusEqual, precipitationTypeEqual, authorIdEqual, principal);
  }

  @Override
  @Transactional
  public FeedDTO updateFeed(UUID feedId, FeedUpdateRequest request) {
    Feed feed = feedRepository.findById(feedId).orElseThrow(
        () -> new NoSuchElementException("존재하지 않는 피드 입니다."));

    feed.updateFeed(request.content());

    feedElasticService.index(feed);

    return feedMapper.toFeedDTO(feed);
  }

  @Override
  @Transactional
  public void deleteFeed(UUID feedId) {
    Feed feed = feedRepository.findById(feedId).orElseThrow(
        () -> new NoSuchElementException("존재하지 않는 피드 입니다."));

    feedElasticService.delete(feedId);

    feedRepository.delete(feed);
  }

}
