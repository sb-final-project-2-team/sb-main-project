package com.codeit.closet.module.feed.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeit.closet.module.cloth.entity.Cloth;
import com.codeit.closet.module.cloth.entity.ClothType;
import com.codeit.closet.module.cloth.repository.ClothRepository;
import com.codeit.closet.module.elastic.dto.FeedSearchResult;
import com.codeit.closet.module.elastic.service.FeedElasticService;
import com.codeit.closet.module.elastic.service.FeedSearchService;
import com.codeit.closet.module.feed.dto.FeedCreateRequest;
import com.codeit.closet.module.feed.dto.FeedDTO;
import com.codeit.closet.module.feed.dto.FeedDTOCursorResponse;
import com.codeit.closet.module.feed.dto.FeedUpdateRequest;
import com.codeit.closet.module.feed.entity.Feed;
import com.codeit.closet.module.feed.mapper.FeedMapper;
import com.codeit.closet.module.feed.repository.FeedRepository;
import com.codeit.closet.module.feed.service.impl.BasicFeedService;
import com.codeit.closet.module.follow.repository.FollowRepository;
import com.codeit.closet.module.user.entity.User;
import com.codeit.closet.module.user.repository.UserRepository;
import com.codeit.closet.module.weather.entity.WeatherData;
import com.codeit.closet.module.weather.repository.WeatherDataRepository;
import com.codeit.closet.module.weather.repository.WeatherRegionRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
@DisplayName("BasicFeedService 테스트")
class BasicFeedServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WeatherRegionRepository weatherRegionRepository;

    @Mock
    private WeatherDataRepository weatherDataRepository;

    @Mock
    private ClothRepository clothRepository;

    @Mock
    private FeedRepository feedRepository;

    @Mock
    private FeedMapper feedMapper;

    @Mock
    private FeedSearchService feedSearchService;

    @Mock
    private FeedElasticService feedElasticService;

    @Mock
    private FollowRepository followRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private BasicFeedService feedService;

    private UUID testUserId;
    private UUID testWeatherId;
    private UUID testClothId;
    private UUID testFeedId;
    private User testUser;
    private WeatherData testWeather;
    private Cloth testCloth;
    private Feed testFeed;
    private FeedDTO testFeedDTO;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testWeatherId = UUID.randomUUID();
        testClothId = UUID.randomUUID();
        testFeedId = UUID.randomUUID();

        // Mock User
        testUser = mock(User.class);
        lenient().when(testUser.getId()).thenReturn(testUserId);
        lenient().when(testUser.getName()).thenReturn("testuser");

        // Mock WeatherData
        testWeather = mock(WeatherData.class);
        lenient().when(testWeather.getId()).thenReturn(testWeatherId);

        // Mock Cloth
        testCloth = mock(Cloth.class);
        lenient().when(testCloth.getId()).thenReturn(testClothId);
        lenient().when(testCloth.getName()).thenReturn("테스트 셔츠");
        lenient().when(testCloth.getType()).thenReturn(ClothType.TOP);

        // Mock Feed
        testFeed = spy(Feed.builder()
                .user(testUser)
                .weather(testWeather)
                .content("테스트 피드")
                .build());
        lenient().when(testFeed.getId()).thenReturn(testFeedId);

        // Mock FeedDTO
        testFeedDTO = mock(FeedDTO.class);
        lenient().when(testFeedDTO.content()).thenReturn("테스트 피드");

        lenient().when(followRepository.findAllByFollowee_Id(any(UUID.class)))
            .thenReturn(List.of());
    }

    @Test
    @DisplayName("피드 생성 성공")
    void createFeed_Success() {
        // given
        FeedCreateRequest request = new FeedCreateRequest(
                testUserId,
                testWeatherId,
                List.of(testClothId),
                "새로운 피드"
        );

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(weatherDataRepository.findById(testWeatherId)).thenReturn(Optional.of(testWeather));
        when(clothRepository.findAllById(List.of(testClothId))).thenReturn(List.of(testCloth));
        when(feedRepository.save(any(Feed.class))).thenReturn(testFeed);
        when(feedMapper.toFeedDTO(testFeed)).thenReturn(testFeedDTO);

        // when
        FeedDTO result = feedService.createFeed(request);

        // then
        assertThat(result).isNotNull();
        verify(userRepository, times(1)).findById(testUserId);
        verify(weatherDataRepository, times(1)).findById(testWeatherId);
        verify(clothRepository, times(1)).findAllById(List.of(testClothId));
        verify(feedRepository, times(1)).save(any(Feed.class));
        verify(feedMapper, times(1)).toFeedDTO(testFeed);

        verify(eventPublisher, never()).publishEvent(any(Object.class));
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 피드 생성 시 예외 발생")
    void createFeed_UserNotFound() {
        // given
        FeedCreateRequest request = new FeedCreateRequest(
                UUID.randomUUID(),
                testWeatherId,
                List.of(testClothId),
                "새로운 피드"
        );

        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> feedService.createFeed(request))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("존재하지 않는 회원 정보입니다");

        verify(userRepository, times(1)).findById(any(UUID.class));
        verify(feedRepository, never()).save(any(Feed.class));
    }

    @Test
    @DisplayName("존재하지 않는 날씨 정보로 피드 생성 시 예외 발생")
    void createFeed_WeatherNotFound() {
        // given
        FeedCreateRequest request = new FeedCreateRequest(
                testUserId,
                UUID.randomUUID(),
                List.of(testClothId),
                "새로운 피드"
        );

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(weatherDataRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> feedService.createFeed(request))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("존재하지 않는 날씨 정보 입니다");

        verify(weatherDataRepository, times(1)).findById(any(UUID.class));
        verify(feedRepository, never()).save(any(Feed.class));
    }

    @Test
    @DisplayName("여러 의상으로 피드 생성 성공")
    void createFeed_WithMultipleClothes() {
        // given
        UUID clothId2 = UUID.randomUUID();
        Cloth cloth2 = mock(Cloth.class);
        lenient().when(cloth2.getId()).thenReturn(clothId2);

        FeedCreateRequest request = new FeedCreateRequest(
                testUserId,
                testWeatherId,
                List.of(testClothId, clothId2),
                "오늘의 코디"
        );

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(weatherDataRepository.findById(testWeatherId)).thenReturn(Optional.of(testWeather));
        when(clothRepository.findAllById(List.of(testClothId, clothId2)))
                .thenReturn(List.of(testCloth, cloth2));
        when(feedRepository.save(any(Feed.class))).thenReturn(testFeed);
        when(feedMapper.toFeedDTO(testFeed)).thenReturn(testFeedDTO);

        // when
        FeedDTO result = feedService.createFeed(request);

        // then
        assertThat(result).isNotNull();
        verify(clothRepository, times(1)).findAllById(List.of(testClothId, clothId2));
    }

    @Test
    @DisplayName("검색 조건이 있는 경우 Elasticsearch 경로를 타고 ID 순서를 유지한다")
    void findFeeds_WithCondition_UsesElasticAndKeepsOrder() {
        // given
        String cursor = null;
        UUID idAfter = null;
        int limit = 3;

        UUID feedId1 = UUID.randomUUID();
        UUID feedId2 = UUID.randomUUID();
        UUID feedId3 = UUID.randomUUID();

        List<UUID> feedIdsFromES = List.of(feedId2, feedId1, feedId3);

        FeedSearchResult searchResult = new FeedSearchResult(
            feedIdsFromES,
            "nextCursor",
            UUID.randomUUID(),
            true,
            100L
        );

        Feed feed1 = mock(Feed.class);
        Feed feed2 = mock(Feed.class);
        Feed feed3 = mock(Feed.class);

        when(feed1.getId()).thenReturn(feedId1);
        when(feed2.getId()).thenReturn(feedId2);
        when(feed3.getId()).thenReturn(feedId3);

        when(feedSearchService.searchFeedIds(
            any(), any(), any(), any(), any(),
            any(), any(), any(), any()
        )).thenReturn(searchResult);

        // ⚠️ 일부러 순서 섞어서 반환
        when(feedRepository.findFeedsByIdIn(feedIdsFromES))
            .thenReturn(new ArrayList<>(List.of(feed1, feed3, feed2)));

        FeedDTO dto1 = mock(FeedDTO.class);
        FeedDTO dto2 = mock(FeedDTO.class);
        FeedDTO dto3 = mock(FeedDTO.class);

        when(feedMapper.toFeedDTOs(any()))
            .thenReturn(List.of(dto2, dto1, dto3));

        // when
        FeedDTOCursorResponse response = feedService.findFeeds(
            cursor,
            idAfter,
            limit,
            "createdAt",
            "DESC",
            "코디",          // 🔥 조건 하나만 줘도 ES 경로
            null,
            null,
            null,
            testUserId
        );

        // then
        assertThat(response.data()).hasSize(3);

        // ES가 준 ID 순서 그대로 유지되는지
        assertThat(response.data()).containsExactly(dto2, dto1, dto3);

        verify(feedSearchService, times(1)).searchFeedIds(
            any(), any(), any(), any(), any(),
            any(), any(), any(), any()
        );

        verify(feedRepository, times(1)).findFeedsByIdIn(feedIdsFromES);

        // 🔒 RDB 직접 조회는 타면 안 됨
        verify(feedRepository, never()).findFeedsByCursor(
            any(), any(), any(), any(), any(),
            any(), any(), any(), any(), any()
        );
    }


    @Test
    @DisplayName("피드 업데이트 성공")
    void updateFeed_Success() {
        // given
        FeedUpdateRequest request = new FeedUpdateRequest("수정된 내용");

        FeedDTO updatedDTO = mock(FeedDTO.class);
        when(updatedDTO.content()).thenReturn("수정된 내용");

        when(feedRepository.findById(testFeedId)).thenReturn(Optional.of(testFeed));
        when(feedMapper.toFeedDTO(testFeed)).thenReturn(updatedDTO);

        // when
        FeedDTO result = feedService.updateFeed(testFeedId, request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo("수정된 내용");
        verify(feedRepository, times(1)).findById(testFeedId);
        verify(testFeed, times(1)).updateFeed("수정된 내용");
        verify(feedMapper, times(1)).toFeedDTO(testFeed);
    }

    @Test
    @DisplayName("존재하지 않는 피드 업데이트 시 예외 발생")
    void updateFeed_NotFound() {
        // given
        UUID nonExistentId = UUID.randomUUID();
        FeedUpdateRequest request = new FeedUpdateRequest("수정된 내용");

        when(feedRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> feedService.updateFeed(nonExistentId, request))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("존재하지 않는 피드 입니다");

        verify(feedRepository, times(1)).findById(nonExistentId);
        verify(feedMapper, never()).toFeedDTO(any(Feed.class));
    }

    @Test
    @DisplayName("피드 삭제 성공")
    void deleteFeed_Success() {
        // given
        when(feedRepository.findById(testFeedId)).thenReturn(Optional.of(testFeed));

        // when
        feedService.deleteFeed(testFeedId);

        // then
        verify(feedRepository, times(1)).findById(testFeedId);
        verify(feedRepository, times(1)).delete(testFeed);
    }

    @Test
    @DisplayName("존재하지 않는 피드 삭제 시 예외 발생")
    void deleteFeed_NotFound() {
        // given
        UUID nonExistentId = UUID.randomUUID();
        when(feedRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> feedService.deleteFeed(nonExistentId))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("존재하지 않는 피드 입니다");

        verify(feedRepository, times(1)).findById(nonExistentId);
        verify(feedRepository, never()).delete(any(Feed.class));
    }

    @Test
    @DisplayName("빈 의상 리스트로 피드 생성")
    void createFeed_WithEmptyClothList() {
        // given
        FeedCreateRequest request = new FeedCreateRequest(
                testUserId,
                testWeatherId,
                List.of(),
                "의상 없는 피드"
        );

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(weatherDataRepository.findById(testWeatherId)).thenReturn(Optional.of(testWeather));
        when(clothRepository.findAllById(List.of())).thenReturn(List.of());
        when(feedRepository.save(any(Feed.class))).thenReturn(testFeed);
        when(feedMapper.toFeedDTO(testFeed)).thenReturn(testFeedDTO);

        // when
        FeedDTO result = feedService.createFeed(request);

        // then
        assertThat(result).isNotNull();
        verify(clothRepository, times(1)).findAllById(List.of());
        verify(feedRepository, times(1)).save(any(Feed.class));
    }

    @Test
    @DisplayName("null 내용으로 피드 업데이트 시 업데이트되지 않음")
    void updateFeed_WithNullContent() {
        // given
        FeedUpdateRequest request = new FeedUpdateRequest(null);

        when(feedRepository.findById(testFeedId)).thenReturn(Optional.of(testFeed));
        when(feedMapper.toFeedDTO(testFeed)).thenReturn(testFeedDTO);

        // when
        FeedDTO result = feedService.updateFeed(testFeedId, request);

        // then
        assertThat(result).isNotNull();
        verify(testFeed, times(1)).updateFeed(null);
        verify(feedMapper, times(1)).toFeedDTO(testFeed);
    }

    @Test
    @DisplayName("내용 없이 피드 생성 성공")
    void createFeed_WithoutContent() {
        // given
        FeedCreateRequest request = new FeedCreateRequest(
                testUserId,
                testWeatherId,
                List.of(testClothId),
                null
        );

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(weatherDataRepository.findById(testWeatherId)).thenReturn(Optional.of(testWeather));
        when(clothRepository.findAllById(List.of(testClothId))).thenReturn(List.of(testCloth));
        when(feedRepository.save(any(Feed.class))).thenReturn(testFeed);
        when(feedMapper.toFeedDTO(testFeed)).thenReturn(testFeedDTO);

        // when
        FeedDTO result = feedService.createFeed(request);

        // then
        assertThat(result).isNotNull();
        verify(feedRepository, times(1)).save(any(Feed.class));
    }
}
