package com.codeit.closet.module.recommendation.algorithm;

import com.codeit.closet.module.cloth.entity.Cloth;
import com.codeit.closet.module.cloth.entity.ClothType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OutfitCombinationGenerator 테스트")
class OutfitCombinationGeneratorTest {

    private OutfitCombinationGenerator generator;

    private Cloth top1;
    private Cloth top2;
    private Cloth bottom1;
    private Cloth bottom2;
    private Cloth outer1;
    private Cloth dress1;

    @BeforeEach
    void setUp() {
        generator = new OutfitCombinationGenerator();

        Instant now = Instant.now();

        top1 = Cloth.builder()
                .id(UUID.randomUUID())
                .name("흰색 셔츠")
                .type(ClothType.TOP)
                .createdAt(now)
                .build();

        top2 = Cloth.builder()
                .id(UUID.randomUUID())
                .name("검정 니트")
                .type(ClothType.TOP)
                .createdAt(now)
                .build();

        bottom1 = Cloth.builder()
                .id(UUID.randomUUID())
                .name("청바지")
                .type(ClothType.BOTTOM)
                .createdAt(now)
                .build();

        bottom2 = Cloth.builder()
                .id(UUID.randomUUID())
                .name("슬랙스")
                .type(ClothType.BOTTOM)
                .createdAt(now)
                .build();

        outer1 = Cloth.builder()
                .id(UUID.randomUUID())
                .name("패딩")
                .type(ClothType.OUTER)
                .createdAt(now)
                .build();

        dress1 = Cloth.builder()
                .id(UUID.randomUUID())
                .name("원피스")
                .type(ClothType.DRESS)
                .createdAt(now)
                .build();
    }

    @Nested
    @DisplayName("generateCombinations - 코디 조합 생성")
    class GenerateCombinationsTest {

        @Test
        @DisplayName("TOP + BOTTOM 기본 조합 생성")
        void shouldGenerateTopBottomCombinations() {
            // Given
            List<Cloth> clothes = List.of(top1, top2, bottom1, bottom2);
            boolean outerRequired = false;
            int maxCombinations = 10;

            // When
            List<List<Cloth>> combinations = generator.generateCombinations(clothes, outerRequired, maxCombinations);

            // Then
            assertThat(combinations).isNotEmpty();
            // 2 tops * 2 bottoms = 4 조합
            assertThat(combinations).hasSize(4);

            // 모든 조합이 TOP + BOTTOM 구성인지 확인
            for (List<Cloth> outfit : combinations) {
                assertThat(outfit).hasSize(2);
                assertThat(outfit.stream().anyMatch(c -> c.getType() == ClothType.TOP)).isTrue();
                assertThat(outfit.stream().anyMatch(c -> c.getType() == ClothType.BOTTOM)).isTrue();
            }
        }

        @Test
        @DisplayName("아우터 필수일 때 아우터 포함 조합만 생성")
        void shouldIncludeOuterWhenRequired() {
            // Given
            List<Cloth> clothes = List.of(top1, bottom1, outer1);
            boolean outerRequired = true;
            int maxCombinations = 10;

            // When
            List<List<Cloth>> combinations = generator.generateCombinations(clothes, outerRequired, maxCombinations);

            // Then
            assertThat(combinations).isNotEmpty();

            // 모든 조합에 아우터 포함
            for (List<Cloth> outfit : combinations) {
                assertThat(outfit.stream().anyMatch(c -> c.getType() == ClothType.OUTER)).isTrue();
            }
        }

        @Test
        @DisplayName("아우터 선택적일 때 아우터 있는/없는 조합 모두 생성")
        void shouldGenerateBothWithAndWithoutOuter() {
            // Given
            List<Cloth> clothes = List.of(top1, bottom1, outer1);
            boolean outerRequired = false;
            int maxCombinations = 10;

            // When
            List<List<Cloth>> combinations = generator.generateCombinations(clothes, outerRequired, maxCombinations);

            // Then
            assertThat(combinations).isNotEmpty();

            // 아우터 있는 조합과 없는 조합 모두 존재
            boolean hasOutfitWithOuter = combinations.stream()
                    .anyMatch(outfit -> outfit.stream().anyMatch(c -> c.getType() == ClothType.OUTER));
            boolean hasOutfitWithoutOuter = combinations.stream()
                    .anyMatch(outfit -> outfit.stream().noneMatch(c -> c.getType() == ClothType.OUTER));

            assertThat(hasOutfitWithOuter).isTrue();
            assertThat(hasOutfitWithoutOuter).isTrue();
        }

        @Test
        @DisplayName("DRESS 단독 조합 생성")
        void shouldGenerateDressCombinations() {
            // Given
            List<Cloth> clothes = List.of(dress1);
            boolean outerRequired = false;
            int maxCombinations = 10;

            // When
            List<List<Cloth>> combinations = generator.generateCombinations(clothes, outerRequired, maxCombinations);

            // Then
            assertThat(combinations).isNotEmpty();
            assertThat(combinations).hasSize(1);
            assertThat(combinations.get(0)).containsExactly(dress1);
        }

        @Test
        @DisplayName("DRESS + OUTER 조합 생성")
        void shouldGenerateDressWithOuterCombinations() {
            // Given
            List<Cloth> clothes = List.of(dress1, outer1);
            boolean outerRequired = false;
            int maxCombinations = 10;

            // When
            List<List<Cloth>> combinations = generator.generateCombinations(clothes, outerRequired, maxCombinations);

            // Then
            assertThat(combinations).isNotEmpty();

            // 원피스만, 원피스+아우터 조합 둘 다 존재
            boolean hasDressOnly = combinations.stream()
                    .anyMatch(outfit -> outfit.size() == 1 && outfit.get(0).getType() == ClothType.DRESS);
            boolean hasDressWithOuter = combinations.stream()
                    .anyMatch(outfit -> outfit.size() == 2);

            assertThat(hasDressOnly).isTrue();
            assertThat(hasDressWithOuter).isTrue();
        }

        @Test
        @DisplayName("maxCombinations 제한 적용")
        void shouldLimitCombinations() {
            // Given
            List<Cloth> clothes = List.of(top1, top2, bottom1, bottom2, outer1);
            boolean outerRequired = false;
            int maxCombinations = 3;

            // When
            List<List<Cloth>> combinations = generator.generateCombinations(clothes, outerRequired, maxCombinations);

            // Then
            assertThat(combinations.size()).isLessThanOrEqualTo(maxCombinations);
        }

        @Test
        @DisplayName("아우터 필수인데 아우터 없으면 빈 조합 반환")
        void shouldReturnEmptyWhenOuterRequiredButNoOuter() {
            // Given
            List<Cloth> clothes = List.of(top1, bottom1, dress1);
            boolean outerRequired = true;
            int maxCombinations = 10;

            // When
            List<List<Cloth>> combinations = generator.generateCombinations(clothes, outerRequired, maxCombinations);

            // Then
            assertThat(combinations).isEmpty();
        }

        @Test
        @DisplayName("TOP만 1개 있을 때 단일 조합 생성")
        void shouldGenerateSingleTopCombination() {
            // Given
            List<Cloth> clothes = List.of(top1);
            boolean outerRequired = false;
            int maxCombinations = 10;

            // When
            List<List<Cloth>> combinations = generator.generateCombinations(clothes, outerRequired, maxCombinations);

            // Then
            assertThat(combinations).hasSize(1);
            assertThat(combinations.get(0)).containsExactly(top1);
        }

        @Test
        @DisplayName("BOTTOM만 1개 있을 때 단일 조합 생성")
        void shouldGenerateSingleBottomCombination() {
            // Given
            List<Cloth> clothes = List.of(bottom1);
            boolean outerRequired = false;
            int maxCombinations = 10;

            // When
            List<List<Cloth>> combinations = generator.generateCombinations(clothes, outerRequired, maxCombinations);

            // Then
            assertThat(combinations).hasSize(1);
            assertThat(combinations.get(0)).containsExactly(bottom1);
        }

        @Test
        @DisplayName("OUTER만 1개 있을 때 단일 조합 생성")
        void shouldGenerateSingleOuterCombination() {
            // Given
            List<Cloth> clothes = List.of(outer1);
            boolean outerRequired = false;
            int maxCombinations = 10;

            // When
            List<List<Cloth>> combinations = generator.generateCombinations(clothes, outerRequired, maxCombinations);

            // Then
            assertThat(combinations).hasSize(1);
            assertThat(combinations.get(0)).containsExactly(outer1);
        }

        @Test
        @DisplayName("TOP + OUTER만 있을 때 조합 생성")
        void shouldGenerateTopWithOuterCombinations() {
            // Given
            List<Cloth> clothes = List.of(top1, outer1);
            boolean outerRequired = false;
            int maxCombinations = 10;

            // When
            List<List<Cloth>> combinations = generator.generateCombinations(clothes, outerRequired, maxCombinations);

            // Then
            assertThat(combinations).isNotEmpty();

            // TOP 단독과 TOP+OUTER 조합 모두 존재
            boolean hasTopOnly = combinations.stream()
                    .anyMatch(outfit -> outfit.size() == 1 && outfit.get(0).getType() == ClothType.TOP);
            boolean hasTopWithOuter = combinations.stream()
                    .anyMatch(outfit -> outfit.size() == 2 &&
                            outfit.stream().anyMatch(c -> c.getType() == ClothType.TOP) &&
                            outfit.stream().anyMatch(c -> c.getType() == ClothType.OUTER));
            boolean hasOuterOnly = combinations.stream()
                    .anyMatch(outfit -> outfit.size() == 1 && outfit.get(0).getType() == ClothType.OUTER);

            assertThat(hasTopOnly).isTrue();
            assertThat(hasTopWithOuter).isTrue();
            assertThat(hasOuterOnly).isTrue();
        }

        @Test
        @DisplayName("아우터 필수일 때 단일 TOP은 아우터와 조합")
        void shouldCombineTopWithOuterWhenOuterRequired() {
            // Given
            List<Cloth> clothes = List.of(top1, outer1);
            boolean outerRequired = true;
            int maxCombinations = 10;

            // When
            List<List<Cloth>> combinations = generator.generateCombinations(clothes, outerRequired, maxCombinations);

            // Then
            assertThat(combinations).isNotEmpty();

            // 모든 조합에 아우터 포함
            for (List<Cloth> outfit : combinations) {
                assertThat(outfit.stream().anyMatch(c -> c.getType() == ClothType.OUTER)).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("isValidCombination - 유효 조합 검증")
    class IsValidCombinationTest {

        @Test
        @DisplayName("TOP + BOTTOM 조합은 유효")
        void shouldReturnTrueForTopAndBottom() {
            // Given
            List<Cloth> outfit = List.of(top1, bottom1);

            // When
            boolean valid = generator.isValidCombination(outfit);

            // Then
            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("DRESS 단독 조합은 유효")
        void shouldReturnTrueForDressAlone() {
            // Given
            List<Cloth> outfit = List.of(dress1);

            // When
            boolean valid = generator.isValidCombination(outfit);

            // Then
            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("TOP만 있는 조합도 유효")
        void shouldReturnTrueForTopOnly() {
            // Given
            List<Cloth> outfit = List.of(top1);

            // When
            boolean valid = generator.isValidCombination(outfit);

            // Then
            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("BOTTOM만 있는 조합도 유효")
        void shouldReturnTrueForBottomOnly() {
            // Given
            List<Cloth> outfit = List.of(bottom1);

            // When
            boolean valid = generator.isValidCombination(outfit);

            // Then
            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("OUTER만 있는 조합도 유효")
        void shouldReturnTrueForOuterOnly() {
            // Given
            List<Cloth> outfit = List.of(outer1);

            // When
            boolean valid = generator.isValidCombination(outfit);

            // Then
            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("빈 조합은 무효")
        void shouldReturnFalseForEmptyOutfit() {
            // Given
            List<Cloth> outfit = List.of();

            // When
            boolean valid = generator.isValidCombination(outfit);

            // Then
            assertThat(valid).isFalse();
        }

        @Test
        @DisplayName("null 조합은 무효")
        void shouldReturnFalseForNullOutfit() {
            // When
            boolean valid = generator.isValidCombination(null);

            // Then
            assertThat(valid).isFalse();
        }
    }

    @Nested
    @DisplayName("canGenerateCombinations - 조합 생성 가능 여부")
    class CanGenerateCombinationsTest {

        @Test
        @DisplayName("TOP + BOTTOM 있으면 조합 생성 가능")
        void shouldReturnTrueWhenTopAndBottomExist() {
            // Given
            List<Cloth> clothes = List.of(top1, bottom1);

            // When
            boolean canGenerate = generator.canGenerateCombinations(clothes);

            // Then
            assertThat(canGenerate).isTrue();
        }

        @Test
        @DisplayName("DRESS 있으면 조합 생성 가능")
        void shouldReturnTrueWhenDressExists() {
            // Given
            List<Cloth> clothes = List.of(dress1);

            // When
            boolean canGenerate = generator.canGenerateCombinations(clothes);

            // Then
            assertThat(canGenerate).isTrue();
        }

        @Test
        @DisplayName("TOP만 있어도 조합 생성 가능")
        void shouldReturnTrueWhenOnlyTopExists() {
            // Given
            List<Cloth> clothes = List.of(top1, top2);

            // When
            boolean canGenerate = generator.canGenerateCombinations(clothes);

            // Then
            assertThat(canGenerate).isTrue();
        }

        @Test
        @DisplayName("BOTTOM만 있어도 조합 생성 가능")
        void shouldReturnTrueWhenOnlyBottomExists() {
            // Given
            List<Cloth> clothes = List.of(bottom1, bottom2);

            // When
            boolean canGenerate = generator.canGenerateCombinations(clothes);

            // Then
            assertThat(canGenerate).isTrue();
        }

        @Test
        @DisplayName("OUTER만 있어도 조합 생성 가능")
        void shouldReturnTrueWhenOnlyOuterExists() {
            // Given
            List<Cloth> clothes = List.of(outer1);

            // When
            boolean canGenerate = generator.canGenerateCombinations(clothes);

            // Then
            assertThat(canGenerate).isTrue();
        }

        @Test
        @DisplayName("빈 목록이면 조합 생성 불가")
        void shouldReturnFalseForEmptyList() {
            // Given
            List<Cloth> clothes = List.of();

            // When
            boolean canGenerate = generator.canGenerateCombinations(clothes);

            // Then
            assertThat(canGenerate).isFalse();
        }
    }
}
