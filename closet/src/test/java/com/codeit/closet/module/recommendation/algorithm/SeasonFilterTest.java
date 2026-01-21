package com.codeit.closet.module.recommendation.algorithm;

import com.codeit.closet.module.cloth.entity.Cloth;
import com.codeit.closet.module.cloth.entity.ClothType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SeasonFilter 테스트")
class SeasonFilterTest {

    private SeasonFilter seasonFilter;

    @BeforeEach
    void setUp() {
        seasonFilter = new SeasonFilter();
    }

    @Nested
    @DisplayName("determinePrimarySeason - 온도에 따른 주 계절 결정")
    class DeterminePrimarySeasonTest {

        @Test
        @DisplayName("0도는 WINTER")
        void shouldReturnWinterForZeroDegree() {
            assertThat(seasonFilter.determinePrimarySeason(0)).isEqualTo("WINTER");
        }

        @Test
        @DisplayName("9도는 WINTER")
        void shouldReturnWinterForNineDegree() {
            assertThat(seasonFilter.determinePrimarySeason(9)).isEqualTo("WINTER");
        }

        @Test
        @DisplayName("10도는 FALL")
        void shouldReturnFallForTenDegree() {
            assertThat(seasonFilter.determinePrimarySeason(10)).isEqualTo("FALL");
        }

        @Test
        @DisplayName("16도는 FALL")
        void shouldReturnFallForSixteenDegree() {
            assertThat(seasonFilter.determinePrimarySeason(16)).isEqualTo("FALL");
        }

        @Test
        @DisplayName("17도는 SPRING")
        void shouldReturnSpringForSeventeenDegree() {
            assertThat(seasonFilter.determinePrimarySeason(17)).isEqualTo("SPRING");
        }

        @Test
        @DisplayName("22도는 SPRING")
        void shouldReturnSpringForTwentyTwoDegree() {
            assertThat(seasonFilter.determinePrimarySeason(22)).isEqualTo("SPRING");
        }

        @Test
        @DisplayName("23도는 SUMMER")
        void shouldReturnSummerForTwentyThreeDegree() {
            assertThat(seasonFilter.determinePrimarySeason(23)).isEqualTo("SUMMER");
        }

        @Test
        @DisplayName("30도는 SUMMER")
        void shouldReturnSummerForThirtyDegree() {
            assertThat(seasonFilter.determinePrimarySeason(30)).isEqualTo("SUMMER");
        }
    }

    @Nested
    @DisplayName("getAllowedSeasons - 허용 계절 목록")
    class GetAllowedSeasonsTest {

        @Test
        @DisplayName("5도, 민감도 3 (보통) → WINTER, FALL 허용")
        void shouldAllowWinterAndFallForNormalSensitivity() {
            // Given
            double temp = 5;
            int sensitivity = 3;

            // When
            Set<String> allowed = seasonFilter.getAllowedSeasons(temp, sensitivity);

            // Then
            assertThat(allowed).containsExactlyInAnyOrder("WINTER", "FALL");
        }

        @Test
        @DisplayName("5도, 민감도 1 (추위 많이 탐) → WINTER만 허용")
        void shouldAllowOnlyWinterForColdSensitivity() {
            // Given
            double temp = 5;
            int sensitivity = 1;

            // When
            Set<String> allowed = seasonFilter.getAllowedSeasons(temp, sensitivity);

            // Then
            assertThat(allowed).containsExactly("WINTER");
        }

        @Test
        @DisplayName("15도, 민감도 3 (보통) → FALL, WINTER, SPRING 허용")
        void shouldAllowFallWinterSpringForMidTemp() {
            // Given
            double temp = 15;
            int sensitivity = 3;

            // When
            Set<String> allowed = seasonFilter.getAllowedSeasons(temp, sensitivity);

            // Then
            assertThat(allowed).containsExactlyInAnyOrder("FALL", "WINTER", "SPRING");
        }

        @Test
        @DisplayName("15도, 민감도 1 (추위 많이 탐) → FALL, WINTER만 허용")
        void shouldAllowFallAndWinterForColdSensitivity() {
            // Given
            double temp = 15;
            int sensitivity = 1;

            // When
            Set<String> allowed = seasonFilter.getAllowedSeasons(temp, sensitivity);

            // Then
            assertThat(allowed).containsExactlyInAnyOrder("FALL", "WINTER");
        }

        @Test
        @DisplayName("20도, 민감도 5 (더위 많이 탐) → SPRING, SUMMER 허용")
        void shouldAllowSpringAndSummerForHotSensitivity() {
            // Given
            double temp = 20;
            int sensitivity = 5;

            // When
            Set<String> allowed = seasonFilter.getAllowedSeasons(temp, sensitivity);

            // Then
            assertThat(allowed).containsExactlyInAnyOrder("SPRING", "SUMMER");
        }

        @Test
        @DisplayName("30도, 민감도 3 (보통) → SUMMER, SPRING 허용")
        void shouldAllowSummerAndSpringForHotWeather() {
            // Given
            double temp = 30;
            int sensitivity = 3;

            // When
            Set<String> allowed = seasonFilter.getAllowedSeasons(temp, sensitivity);

            // Then
            assertThat(allowed).containsExactlyInAnyOrder("SUMMER", "SPRING");
        }
    }

    @Nested
    @DisplayName("isClothAllowed - 의상 허용 여부")
    class IsClothAllowedTest {

        @Test
        @DisplayName("계절 속성이 없으면 허용")
        void shouldAllowClothWithoutSeasonAttribute() {
            // Given
            Cloth cloth = createCloth(ClothType.TOP);
            Set<String> allowedSeasons = Set.of("WINTER");
            Map<UUID, Map<String, String>> attributeMaps = new HashMap<>();

            // When
            boolean allowed = seasonFilter.isClothAllowed(cloth, 5, allowedSeasons, attributeMaps);

            // Then
            assertThat(allowed).isTrue();
        }

        @Test
        @DisplayName("WINTER 옷은 WINTER 허용 시 통과")
        void shouldAllowWinterClothWhenWinterAllowed() {
            // Given
            Cloth cloth = createCloth(ClothType.TOP);
            Set<String> allowedSeasons = Set.of("WINTER", "FALL");
            Map<UUID, Map<String, String>> attributeMaps = createAttributeMap(cloth.getId(), "WINTER");

            // When
            boolean allowed = seasonFilter.isClothAllowed(cloth, 5, allowedSeasons, attributeMaps);

            // Then
            assertThat(allowed).isTrue();
        }

        @Test
        @DisplayName("SUMMER 옷은 WINTER만 허용 시 거부")
        void shouldRejectSummerClothWhenOnlyWinterAllowed() {
            // Given
            Cloth cloth = createCloth(ClothType.TOP);
            Set<String> allowedSeasons = Set.of("WINTER");
            Map<UUID, Map<String, String>> attributeMaps = createAttributeMap(cloth.getId(), "SUMMER");

            // When
            boolean allowed = seasonFilter.isClothAllowed(cloth, 5, allowedSeasons, attributeMaps);

            // Then
            assertThat(allowed).isFalse();
        }
    }

    @Nested
    @DisplayName("아우터 특별 규칙")
    class OuterSpecialRuleTest {

        @Test
        @DisplayName("겨울(5도)에 WINTER 아우터는 허용")
        void shouldAllowWinterOuterInWinter() {
            // Given
            Cloth outer = createCloth(ClothType.OUTER);
            Set<String> allowedSeasons = Set.of("WINTER", "FALL");
            Map<UUID, Map<String, String>> attributeMaps = createAttributeMap(outer.getId(), "WINTER");

            // When
            boolean allowed = seasonFilter.isClothAllowed(outer, 5, allowedSeasons, attributeMaps);

            // Then
            assertThat(allowed).isTrue();
        }

        @Test
        @DisplayName("겨울(5도)에 FALL 아우터는 거부")
        void shouldRejectFallOuterInWinter() {
            // Given
            Cloth outer = createCloth(ClothType.OUTER);
            Set<String> allowedSeasons = Set.of("WINTER", "FALL");
            Map<UUID, Map<String, String>> attributeMaps = createAttributeMap(outer.getId(), "FALL");

            // When
            boolean allowed = seasonFilter.isClothAllowed(outer, 5, allowedSeasons, attributeMaps);

            // Then
            assertThat(allowed).isFalse();
        }

        @Test
        @DisplayName("겨울(5도)에 FALL 상의는 허용 (아우터 규칙은 아우터만 적용)")
        void shouldAllowFallTopInWinter() {
            // Given
            Cloth top = createCloth(ClothType.TOP);
            Set<String> allowedSeasons = Set.of("WINTER", "FALL");
            Map<UUID, Map<String, String>> attributeMaps = createAttributeMap(top.getId(), "FALL");

            // When
            boolean allowed = seasonFilter.isClothAllowed(top, 5, allowedSeasons, attributeMaps);

            // Then
            assertThat(allowed).isTrue();
        }

        @Test
        @DisplayName("간절기(15도)에 FALL 아우터는 허용")
        void shouldAllowFallOuterInMildWeather() {
            // Given
            Cloth outer = createCloth(ClothType.OUTER);
            Set<String> allowedSeasons = Set.of("FALL", "SPRING");
            Map<UUID, Map<String, String>> attributeMaps = createAttributeMap(outer.getId(), "FALL");

            // When
            boolean allowed = seasonFilter.isClothAllowed(outer, 15, allowedSeasons, attributeMaps);

            // Then
            assertThat(allowed).isTrue();
        }
    }

    @Nested
    @DisplayName("filterWithExpandedSeasons - 확장 계절 필터링")
    class FilterWithExpandedSeasonsTest {

        @Test
        @DisplayName("5도(WINTER)에서 확장 시 WINTER, FALL, SPRING 옷 허용")
        void shouldExpandToIncludeSpringFromWinter() {
            // Given: WINTER(5도) → getAllowedSeasons(5, 3) = {WINTER, FALL}
            // 확장 후 FALL의 인접 계절인 SPRING도 추가되어야 함
            Cloth winterTop = createCloth(ClothType.TOP);
            Cloth fallBottom = createCloth(ClothType.BOTTOM);
            Cloth springDress = createCloth(ClothType.DRESS);
            Cloth summerShirt = createCloth(ClothType.TOP);

            List<Cloth> clothes = List.of(winterTop, fallBottom, springDress, summerShirt);

            Map<UUID, Map<String, String>> attributeMaps = new HashMap<>();
            attributeMaps.put(winterTop.getId(), Map.of("SEASON", "WINTER"));
            attributeMaps.put(fallBottom.getId(), Map.of("SEASON", "FALL"));
            attributeMaps.put(springDress.getId(), Map.of("SEASON", "SPRING"));
            attributeMaps.put(summerShirt.getId(), Map.of("SEASON", "SUMMER"));

            // When
            List<Cloth> filtered = seasonFilter.filterWithExpandedSeasons(clothes, 5, attributeMaps);

            // Then: WINTER, FALL, SPRING 옷은 허용, SUMMER는 제외
            assertThat(filtered).hasSize(3);
            assertThat(filtered).contains(winterTop, fallBottom, springDress);
            assertThat(filtered).doesNotContain(summerShirt);
        }

        @Test
        @DisplayName("25도(SUMMER)에서 확장 시 SUMMER, SPRING, FALL 옷 허용")
        void shouldExpandToIncludeFallFromSummer() {
            // Given: SUMMER(25도) → getAllowedSeasons(25, 3) = {SUMMER, SPRING}
            // 확장 후 SPRING의 인접 계절인 FALL도 추가되어야 함
            Cloth summerTop = createCloth(ClothType.TOP);
            Cloth springBottom = createCloth(ClothType.BOTTOM);
            Cloth fallDress = createCloth(ClothType.DRESS);
            Cloth winterShirt = createCloth(ClothType.TOP);

            List<Cloth> clothes = List.of(summerTop, springBottom, fallDress, winterShirt);

            Map<UUID, Map<String, String>> attributeMaps = new HashMap<>();
            attributeMaps.put(summerTop.getId(), Map.of("SEASON", "SUMMER"));
            attributeMaps.put(springBottom.getId(), Map.of("SEASON", "SPRING"));
            attributeMaps.put(fallDress.getId(), Map.of("SEASON", "FALL"));
            attributeMaps.put(winterShirt.getId(), Map.of("SEASON", "WINTER"));

            // When
            List<Cloth> filtered = seasonFilter.filterWithExpandedSeasons(clothes, 25, attributeMaps);

            // Then: SUMMER, SPRING, FALL 옷은 허용, WINTER는 제외
            assertThat(filtered).hasSize(3);
            assertThat(filtered).contains(summerTop, springBottom, fallDress);
            assertThat(filtered).doesNotContain(winterShirt);
        }

        @Test
        @DisplayName("15도(FALL)에서 확장 시 모든 계절 옷 허용")
        void shouldExpandToAllSeasonsFromFall() {
            // Given: FALL(15도) → getAllowedSeasons(15, 3) = {FALL, WINTER, SPRING}
            // 확장 후 WINTER→FALL(이미있음), SPRING→SUMMER,FALL 추가되어 모든 계절 허용
            Cloth winterTop = createCloth(ClothType.TOP);
            Cloth fallBottom = createCloth(ClothType.BOTTOM);
            Cloth springDress = createCloth(ClothType.DRESS);
            Cloth summerShirt = createCloth(ClothType.TOP);

            List<Cloth> clothes = List.of(winterTop, fallBottom, springDress, summerShirt);

            Map<UUID, Map<String, String>> attributeMaps = new HashMap<>();
            attributeMaps.put(winterTop.getId(), Map.of("SEASON", "WINTER"));
            attributeMaps.put(fallBottom.getId(), Map.of("SEASON", "FALL"));
            attributeMaps.put(springDress.getId(), Map.of("SEASON", "SPRING"));
            attributeMaps.put(summerShirt.getId(), Map.of("SEASON", "SUMMER"));

            // When
            List<Cloth> filtered = seasonFilter.filterWithExpandedSeasons(clothes, 15, attributeMaps);

            // Then: 모든 계절 옷 허용
            assertThat(filtered).hasSize(4);
            assertThat(filtered).contains(winterTop, fallBottom, springDress, summerShirt);
        }
    }

    @Nested
    @DisplayName("filterByAllowedSeasons - 리스트 필터링")
    class FilterByAllowedSeasonsTest {

        @Test
        @DisplayName("5도, 민감도 3에서 WINTER/FALL 옷만 필터링")
        void shouldFilterToWinterAndFallClothes() {
            // Given
            Cloth winterTop = createCloth(ClothType.TOP);
            Cloth fallBottom = createCloth(ClothType.BOTTOM);
            Cloth summerDress = createCloth(ClothType.DRESS);

            List<Cloth> clothes = List.of(winterTop, fallBottom, summerDress);

            Map<UUID, Map<String, String>> attributeMaps = new HashMap<>();
            attributeMaps.put(winterTop.getId(), Map.of("SEASON", "WINTER"));
            attributeMaps.put(fallBottom.getId(), Map.of("SEASON", "FALL"));
            attributeMaps.put(summerDress.getId(), Map.of("SEASON", "SUMMER"));

            // When
            List<Cloth> filtered = seasonFilter.filterByAllowedSeasons(clothes, 5, 3, attributeMaps);

            // Then
            assertThat(filtered).hasSize(2);
            assertThat(filtered).contains(winterTop, fallBottom);
            assertThat(filtered).doesNotContain(summerDress);
        }

        @Test
        @DisplayName("계절 속성 없는 옷도 포함")
        void shouldIncludeClothesWithoutSeasonAttribute() {
            // Given
            Cloth winterTop = createCloth(ClothType.TOP);
            Cloth noSeasonBottom = createCloth(ClothType.BOTTOM);

            List<Cloth> clothes = List.of(winterTop, noSeasonBottom);

            Map<UUID, Map<String, String>> attributeMaps = new HashMap<>();
            attributeMaps.put(winterTop.getId(), Map.of("SEASON", "WINTER"));
            // noSeasonBottom은 속성 없음

            // When
            List<Cloth> filtered = seasonFilter.filterByAllowedSeasons(clothes, 5, 3, attributeMaps);

            // Then
            assertThat(filtered).hasSize(2);
            assertThat(filtered).contains(winterTop, noSeasonBottom);
        }
    }

    // 테스트 헬퍼 메서드
    private Cloth createCloth(ClothType type) {
        return Cloth.builder()
                .id(UUID.randomUUID())
                .type(type)
                .name("Test " + type.name())
                .build();
    }

    private Map<UUID, Map<String, String>> createAttributeMap(UUID clothId, String season) {
        Map<UUID, Map<String, String>> attributeMaps = new HashMap<>();
        attributeMaps.put(clothId, Map.of("SEASON", season));
        return attributeMaps;
    }
}
