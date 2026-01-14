package com.codeit.closet.module.recommendation.service.impl;

import com.codeit.closet.module.cloth.dto.ClothAttributeValueDTO;
import com.codeit.closet.module.cloth.entity.Cloth;
import com.codeit.closet.module.cloth.repository.ClothRepository;
import com.codeit.closet.module.recommendation.algorithm.OutfitCombinationGenerator;
import com.codeit.closet.module.recommendation.algorithm.RecommendationAttributeLoader;
import com.codeit.closet.module.recommendation.algorithm.RecommendationScorer;
import com.codeit.closet.module.recommendation.algorithm.TemperatureClothMatcher;
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
 * - 온도(0-40점) + 강수(0-15점) + 속성(0-30점) = 총 0-85점 범위
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
    private final RecommendationAttributeLoader attributeLoader;

    @Override
    @Transactional(readOnly = true)
    public RecommendationResponse getRecommendations(UUID weatherId, UUID userId, int limit) {
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
        Map<UUID, Map<String, String>> attributeMaps = attributeLoader.loadAttributeMaps(clothIds);
        // 3-2. 속성 DTO 맵 로드 (응답용)
        Map<UUID, List<ClothAttributeValueDTO>> attributeDTOs = attributeLoader.loadAttributeDTOs(clothIds);

        // 4. 코디 조합 생성 가능 여부 확인
        if (!combinationGenerator.canGenerateCombinations(clothes)) {
            throw new InsufficientClothesException(
                    "추천 가능한 코디 조합을 만들 수 없습니다. 상의와 하의를 등록해주세요.");
        }

        // 5. 체감온도 계산 및 아우터 필요 여부 판단
        double adjustedTemp = recommendationScorer.getAdjustedTemperature(
                weather.getTemperatureCurrent(), user.getTemperatureSensitivity());
        boolean outerRequired = temperatureClothMatcher.isOuterRequired(adjustedTemp);

        // 6. 코디 조합 생성
        List<List<Cloth>> combinations = combinationGenerator.generateCombinations(
                clothes, outerRequired, limit * 3);

        // 7. 각 조합 점수 계산 및 정렬
        List<ScoredOutfit> scoredOutfits = combinations.stream()
                .map(outfit -> new ScoredOutfit(
                        outfit,
                        recommendationScorer.calculateOutfitScore(outfit, weather, user.getTemperatureSensitivity(), attributeMaps)
                ))
                .sorted(Comparator.comparingInt(ScoredOutfit::score).reversed())
                .limit(limit)
                .toList();

        // 8. 첫 번째 추천 코디를 DTO로 변환 (프론트엔드 명세 맞춤)
        List<RecommendationClothDTO> recommendedClothes = new ArrayList<>();
        if (!scoredOutfits.isEmpty()) {
            ScoredOutfit topScored = scoredOutfits.get(0);
            for (Cloth cloth : topScored.outfit()) {
                recommendedClothes.add(new RecommendationClothDTO(
                        cloth.getId(),
                        cloth.getName(),
                        cloth.getBinaryContent() != null ? cloth.getBinaryContent().getFileUrl() : null,
                        cloth.getType().name(),
                        attributeDTOs.getOrDefault(cloth.getId(), List.of())
                ));
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
