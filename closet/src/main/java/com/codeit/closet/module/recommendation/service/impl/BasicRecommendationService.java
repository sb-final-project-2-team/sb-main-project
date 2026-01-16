package com.codeit.closet.module.recommendation.service.impl;

import com.codeit.closet.module.cloth.dto.ClothAttributeValueDTO;
import com.codeit.closet.module.cloth.entity.Cloth;
import com.codeit.closet.module.cloth.repository.ClothAttributeQueryRepository;
import com.codeit.closet.module.cloth.repository.ClothRepository;
import com.codeit.closet.module.recommendation.algorithm.OutfitCombinationGenerator;
import com.codeit.closet.module.recommendation.algorithm.RecommendationScorer;
import com.codeit.closet.module.recommendation.algorithm.SeasonFilter;
import com.codeit.closet.module.recommendation.algorithm.TemperatureClothMatcher;
import com.codeit.closet.module.recommendation.mapper.RecommendationMapper;
import com.codeit.closet.module.recommendation.dto.RecommendationClothDTO;
import com.codeit.closet.module.recommendation.dto.RecommendationResponse;
import com.codeit.closet.module.recommendation.exception.InsufficientClothesException;
import com.codeit.closet.module.recommendation.exception.UserWeatherNotSetException;
import com.codeit.closet.module.recommendation.exception.WeatherNotFoundException;
import com.codeit.closet.module.recommendation.service.RecommendationService;
import com.codeit.closet.module.user.entity.User;
import com.codeit.closet.module.user.repository.UserRepository;
import com.codeit.closet.module.weather.entity.WeatherData;
import com.codeit.closet.module.weather.repository.WeatherDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * 날씨 기반 의상 추천 서비스 구현체
 * - 온도(0-40점) + 강수(0-15점) + 속성(0-30점) = 최대 85점 (0-100 범위로 클램핑)
 * - 체감온도 민감도(1-5)에 따라 ±2도 보정 적용
 */
@Service
@RequiredArgsConstructor
public class BasicRecommendationService implements RecommendationService {

    private final WeatherDataRepository weatherDataRepository;
    private final UserRepository userRepository;
    private final ClothRepository clothRepository;

    private final TemperatureClothMatcher temperatureClothMatcher;
    private final OutfitCombinationGenerator combinationGenerator;
    private final RecommendationScorer recommendationScorer;
    private final ClothAttributeQueryRepository clothAttributeQueryRepository;
    private final RecommendationMapper recommendationMapper;
    private final SeasonFilter seasonFilter;

    @Override
    @Transactional(readOnly = true)
    public RecommendationResponse getRecommendations(UUID weatherId, UUID userId, int limit) {
        // 0. 파라미터 사전 검증
        if (limit <= 0) {
            throw new IllegalArgumentException("추천 개수(limit)는 1 이상이어야 합니다.");
        }

        // 1. 사용자 정보 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 회원입니다."));

        // 2. 날씨 데이터 조회
        WeatherData weather = resolveWeatherData(weatherId, user);

        // 3. 사용자 옷장 조회
        List<Cloth> clothes = clothRepository.findAllByOwner_Id(userId);
        if (clothes.isEmpty()) {
            throw new InsufficientClothesException("옷장에 등록된 의상이 없습니다.");
        }

        // 3-1. 속성 맵 일괄 로드 (N+1 방지)
        List<UUID> clothIds = clothes.stream().map(Cloth::getId).toList();
        Map<UUID, Map<String, String>> attributeMaps = clothAttributeQueryRepository.findAttributeMapsByClothIds(clothIds);
        // 3-2. 속성 DTO 맵 로드 (응답용)
        Map<UUID, List<ClothAttributeValueDTO>> attributeDTOs = clothAttributeQueryRepository.findAttributeDTOsByClothIds(clothIds);

        // 4. 체감온도 계산 (필터링에 필요)
        int sensitivity = user.getTemperatureSensitivity();
        double adjustedTemp = recommendationScorer.getAdjustedTemperature(
                weather.getTemperatureCurrent(), sensitivity);

        // 5. 계절 기반 필터링 적용
        List<Cloth> filteredClothes = seasonFilter.filterByAllowedSeasons(
                clothes, adjustedTemp, sensitivity, attributeMaps);

        // 5-1. 필터링 후 의상이 부족하면 확장 필터링 시도
        if (!combinationGenerator.canGenerateCombinations(filteredClothes)) {
            filteredClothes = seasonFilter.filterWithExpandedSeasons(
                    clothes, adjustedTemp, attributeMaps);
        }

        // 6. 코디 조합 생성 가능 여부 확인 (필터링된 의상으로)
        if (!combinationGenerator.canGenerateCombinations(filteredClothes)) {
            throw new InsufficientClothesException(
                    "추천 가능한 코디 조합을 만들 수 없습니다. 현재 날씨에 맞는 계절 의상을 등록해주세요.");
        }

        // 7. 아우터 필요 여부 판단
        boolean outerRequired = temperatureClothMatcher.isOuterRequired(adjustedTemp);

        // 8. 코디 조합 생성 (필터링된 의상으로)
        List<List<Cloth>> combinations = combinationGenerator.generateCombinations(
                filteredClothes, outerRequired, limit * 3);

        // 9. 각 조합 점수 계산 및 정렬
        List<ScoredOutfit> scoredOutfits = combinations.stream()
                .map(outfit -> new ScoredOutfit(
                        outfit,
                        recommendationScorer.calculateOutfitScore(outfit, weather, sensitivity, attributeMaps)
                ))
                .sorted(Comparator.comparingInt(ScoredOutfit::score).reversed())
                .limit(limit)
                .toList();

        // 10. 첫 번째 추천 코디를 DTO로 변환 (MapStruct 사용)
        List<RecommendationClothDTO> recommendedClothes = new ArrayList<>();
        if (!scoredOutfits.isEmpty()) {
            ScoredOutfit topScored = scoredOutfits.get(0);
            for (Cloth cloth : topScored.outfit()) {
                List<ClothAttributeValueDTO> attrs = attributeDTOs.getOrDefault(cloth.getId(), List.of());
                recommendedClothes.add(recommendationMapper.toRecommendationClothDTO(cloth, attrs));
            }
        }

        return new RecommendationResponse(weather.getId(), userId, recommendedClothes);
    }

    @Override
    @Transactional(readOnly = true)
    public RecommendationResponse getRecommendationsWithFeedReference(
            UUID weatherId, UUID userId, boolean includePopularFeeds, int limit) {
        // 2차 구현: 피드 기반 추천 로직
        // 현재는 기본 추천과 동일하게 동작
        return getRecommendations(weatherId, userId, limit);
    }

    /**
     * weatherId가 있으면 해당 데이터 조회,
     * 없으면 사용자 설정 지역의 최신 날씨 데이터 조회
     */
    private WeatherData resolveWeatherData(UUID weatherId, User user) {
        if (weatherId != null) {
            return weatherDataRepository.findById(weatherId)
                    .orElseThrow(() -> new WeatherNotFoundException(weatherId));
        }

        // 사용자 설정 지역에서 최신 날씨 조회
        if (user.getWeather() == null) {
            throw new UserWeatherNotSetException(
                    "날씨 지역이 설정되지 않았습니다. 프로필에서 지역을 설정해주세요.");
        }

        return weatherDataRepository.findLatestByWeatherRegionId(user.getWeather().getId())
                .orElseThrow(() -> new WeatherNotFoundException(user.getWeather().getId()));
    }

    /**
     * 점수가 매겨진 코디 조합 레코드
     */
    private record ScoredOutfit(List<Cloth> outfit, int score) {}
}
