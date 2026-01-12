package com.codeit.closet.module.feed.service.impl;

import com.codeit.closet.module.cloth.entity.Cloth;
import com.codeit.closet.module.cloth.repository.ClothRepository;
import com.codeit.closet.module.feed.dto.FeedCreateRequest;
import com.codeit.closet.module.feed.dto.FeedDTO;
import com.codeit.closet.module.feed.dto.FeedDTOCursorResponse;
import com.codeit.closet.module.feed.dto.FeedUpdateRequest;
import com.codeit.closet.module.feed.entity.Feed;
import com.codeit.closet.module.feed.mapper.FeedMapper;
import com.codeit.closet.module.feed.repository.FeedRepository;
import com.codeit.closet.module.feed.service.FeedService;
import com.codeit.closet.module.user.entity.User;
import com.codeit.closet.module.user.repository.UserRepository;
import com.codeit.closet.module.weather.entity.PrecipitationType;
import com.codeit.closet.module.weather.entity.SkyStatus;
import com.codeit.closet.module.weather.entity.WeatherRegion;
import com.codeit.closet.module.weather.repository.WeatherRegionRepository;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BasicFeedService implements FeedService {

  private final UserRepository userRepository;
  private final WeatherRegionRepository weatherRegionRepository;
  private final ClothRepository clothRepository;
  private final FeedRepository feedRepository;
  private final FeedMapper feedMapper;

  @Override
  @Transactional
  public FeedDTO createFeed(FeedCreateRequest request) {

    User user = userRepository.findById(request.authorId())
        .orElseThrow(() -> new NoSuchElementException("존재하지 않는 회원 정보입니다."));

    // 생성을 하는쪽으로 가야함.
    WeatherRegion weatherRegion = weatherRegionRepository.findById(request.weatherId())
        .orElseThrow(() -> new NoSuchElementException("존재하지 않는 날씨 정보 입니다."));

    // 나만의 전체 Clothe가 아닌 Ootd에서 추천해준 쪽으로 처리해야한다.
    List<Cloth> clothList =
        clothRepository.findAllById(request.clothesIds());

    Feed feed = Feed.builder()
        .user(user)
        .weather(weatherRegion)
        .content(request.content())
        .build();

    for (Cloth clothes : clothList) {
      feed.addOotd(clothes);
    }

    Feed saved = feedRepository.save(feed);

    return feedMapper.toFeedDTO(saved);
  }

  @Override
  @Transactional(readOnly = true)
  public FeedDTOCursorResponse findFeeds(String cursor, UUID idAfter, Integer limit, String sortBy,
      String sortDirection, String keywordLike, SkyStatus skyStatusEqual,
      PrecipitationType precipitationTypeEqual, UUID authorIdEqual, UUID principal) {

    return feedRepository.findFeedsByCursor(cursor, idAfter, limit, sortBy, sortDirection,
        keywordLike, skyStatusEqual, precipitationTypeEqual, authorIdEqual, principal);
  }

  @Override
  @Transactional
  public FeedDTO updateFeed(UUID feedId, FeedUpdateRequest request) {
    Feed feed = feedRepository.findById(feedId).orElseThrow(
        () -> new NoSuchElementException("존재하지 않는 피드 입니다."));

    feed.updateFeed(request.content());

    return feedMapper.toFeedDTO(feed);
  }

  @Override
  @Transactional
  public void deleteFeed(UUID feedId) {
    Feed feed = feedRepository.findById(feedId).orElseThrow(
        () -> new NoSuchElementException("존재하지 않는 피드 입니다."));

    feedRepository.delete(feed);
  }
}
