package com.codeit.closet.module.feed.service.impl;

import com.codeit.closet.module.clothes.entity.Clothes;
import com.codeit.closet.module.clothes.repository.ClothesRepository;
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
  private final ClothesRepository clothesRepository;
  private final FeedRepository feedRepository;
  private final FeedMapper feedMapper;

  @Override
  @Transactional
  public FeedDTO createFeed(FeedCreateRequest request) {

    User user = userRepository.findById(request.authorId())
        .orElseThrow(() -> new NoSuchElementException("존재하지 않는 회원 정보입니다."));

    WeatherRegion weatherRegion = weatherRegionRepository.findById(request.weatherId())
        .orElseThrow(() -> new NoSuchElementException("존재하지 않는 날씨 정보 입니다."));

    List<Clothes> clothesList =
        clothesRepository.findAllById(request.clothesIds());

    Feed feed = Feed.builder()
        .user(user)
        .weather(weatherRegion)
        .content(request.content())
        .build();

    for (Clothes clothes : clothesList) {
      feed.addOotd(clothes);
    }

    Feed saved = feedRepository.save(feed);

    return feedMapper.toDTO(saved);
  }

  @Override
  @Transactional(readOnly = true)
  public FeedDTOCursorResponse findFeeds(String cursor, UUID idAfter, Integer limit, String sortBy,
      String sortDirection, String keywordLike, SkyStatus skyStatusEqual,
      PrecipitationType precipitationTypeEqual, UUID authorIdEqual) {

    return feedRepository.findFeedsByCursor(cursor, idAfter, limit, sortBy, sortDirection,
        keywordLike, skyStatusEqual, precipitationTypeEqual, authorIdEqual);
  }

  @Override
  @Transactional
  public FeedDTO updateFeed(UUID feedId, FeedUpdateRequest request) {
    Feed feed = feedRepository.findById(feedId).orElseThrow(
        () -> new NoSuchElementException("존재하지 않는 피드 입니다."));

    feed.updateFeed(request.content());

    return feedMapper.toDTO(feed);
  }

  @Override
  @Transactional
  public void deleteFeed(UUID feedId) {
    Feed feed = feedRepository.findById(feedId).orElseThrow(
        () -> new NoSuchElementException("존재하지 않는 피드 입니다."));

    feedRepository.delete(feed);
  }
}
