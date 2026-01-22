package com.codeit.closet.module.cloth.repository.impl;

import com.codeit.closet.module.cloth.dto.ClothDTOCursorResponse;
import com.codeit.closet.module.cloth.entity.Cloth;
import com.codeit.closet.module.cloth.entity.ClothType;
import com.codeit.closet.module.cloth.repository.ClothQueryRepository;
import com.codeit.closet.module.cloth.repository.ClothRepository;
import com.codeit.closet.module.user.entity.User;
import com.codeit.closet.module.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DisplayName("ClothQueryRepositoryImpl 통합 테스트")
class ClothQueryRepositoryImplTest {

    @Autowired
    @Qualifier("clothQueryRepositoryImpl")
    private ClothQueryRepository clothQueryRepository;

    @Autowired
    private ClothRepository clothRepository;

    @Autowired
    private UserRepository userRepository;

    private User owner1;
    private User owner2;
    private Cloth cloth1;
    private Cloth cloth2;
    private Cloth cloth3;
    private Cloth cloth4;
    private Cloth cloth5;

    @BeforeEach
    void setUp() throws InterruptedException {
        // 소유자 생성
        owner1 = User.builder()
                .email("owner1@example.com")
                .password("password123")
                .name("owner1")
                .build();
        userRepository.save(owner1);
        userRepository.flush();

        owner2 = User.builder()
                .email("owner2@example.com")
                .password("password123")
                .name("owner2")
                .build();
        userRepository.save(owner2);
        userRepository.flush();

        // owner1의 옷 생성 (시간차를 두고 생성)
        cloth1 = Cloth.builder()
                .owner(owner1)
                .name("Blue Jeans")
                .type(ClothType.BOTTOM)
                .build();
        clothRepository.save(cloth1);
        clothRepository.flush();
        Thread.sleep(10);

        cloth2 = Cloth.builder()
                .owner(owner1)
                .name("White T-Shirt")
                .type(ClothType.TOP)
                .build();
        clothRepository.save(cloth2);
        clothRepository.flush();
        Thread.sleep(10);

        cloth3 = Cloth.builder()
                .owner(owner1)
                .name("Black Jacket")
                .type(ClothType.OUTER)
                .build();
        clothRepository.save(cloth3);
        clothRepository.flush();
        Thread.sleep(10);

        cloth4 = Cloth.builder()
                .owner(owner1)
                .name("Red Dress")
                .type(ClothType.DRESS)
                .build();
        clothRepository.save(cloth4);
        clothRepository.flush();
        Thread.sleep(10);

        // owner2의 옷
        cloth5 = Cloth.builder()
                .owner(owner2)
                .name("Gray Sneakers")
                .type(ClothType.SHOES)
                .build();
        clothRepository.save(cloth5);
        clothRepository.flush();
    }

    @Test
    @DisplayName("커서 없이 기본 페이징 조회 - DESC 정렬")
    void findClothsByCursor_WithoutCursor_Descending() {
        // when
        ClothDTOCursorResponse response = clothQueryRepository.findClothsByCursor(
                owner1.getId(), null, null, 3, "createdAt", "desc", null
        );

        // then
        assertThat(response.data()).hasSize(3);
        assertThat(response.hasNext()).isTrue();
        assertThat(response.nextCursor()).isNotNull();
        assertThat(response.totalCount()).isEqualTo(4); // owner1의 옷 4개
        assertThat(response.sortBy()).isEqualTo("createdAt");
        assertThat(response.sortDirection()).isEqualTo("desc");

        // 최신 옷이 먼저 나와야 함 (Red Dress -> Black Jacket -> White T-Shirt)
        assertThat(response.data().get(0).name()).isEqualTo("Red Dress");
    }

    @Test
    @DisplayName("커서 없이 기본 페이징 조회 - ASC 정렬")
    void findClothsByCursor_WithoutCursor_Ascending() {
        // when
        ClothDTOCursorResponse response = clothQueryRepository.findClothsByCursor(
                owner1.getId(), null, null, 3, "createdAt", "asc", null
        );

        // then
        assertThat(response.data()).hasSize(3);
        assertThat(response.hasNext()).isTrue();
        assertThat(response.sortDirection()).isEqualTo("asc");

        // 가장 오래된 옷이 먼저 (Blue Jeans)
        assertThat(response.data().get(0).name()).isEqualTo("Blue Jeans");
    }

    @Test
    @DisplayName("커서를 사용한 다음 페이지 조회")
    void findClothsByCursor_WithCursor() {
        // given - 첫 페이지 조회
        ClothDTOCursorResponse firstPage = clothQueryRepository.findClothsByCursor(
                owner1.getId(), null, null, 2, "createdAt", "desc", null
        );

        assertThat(firstPage.data()).hasSize(2);
        assertThat(firstPage.hasNext()).isTrue();

        String cursor = firstPage.nextCursor();

        // when - 두 번째 페이지 조회
        ClothDTOCursorResponse secondPage = clothQueryRepository.findClothsByCursor(
                owner1.getId(), cursor, null, 2, "createdAt", "desc", null
        );

        // then
        assertThat(secondPage.data()).hasSizeGreaterThan(0);
        assertThat(secondPage.data().get(0).id()).isNotEqualTo(firstPage.data().get(0).id());
        assertThat(secondPage.data().get(0).id()).isNotEqualTo(firstPage.data().get(1).id());
    }

    @Test
    @DisplayName("타입 필터 조회 - TOP")
    void findClothsByCursor_FilterByType() {
        // when
        ClothDTOCursorResponse response = clothQueryRepository.findClothsByCursor(
                owner1.getId(), null, null, 20, "createdAt", "desc", "TOP"
        );

        // then
        assertThat(response.data()).hasSize(1);
        assertThat(response.data()).allMatch(dto -> dto.type().equals("TOP"));
        assertThat(response.data().get(0).name()).isEqualTo("White T-Shirt");
    }

    @Test
    @DisplayName("타입 필터 조회 - OUTER")
    void findClothsByCursor_FilterByOuter() {
        // when
        ClothDTOCursorResponse response = clothQueryRepository.findClothsByCursor(
                owner1.getId(), null, null, 20, "createdAt", "desc", "OUTER"
        );

        // then
        assertThat(response.data()).hasSize(1);
        assertThat(response.data()).allMatch(dto -> dto.type().equals("OUTER"));
        assertThat(response.data().get(0).name()).isEqualTo("Black Jacket");
    }

    @Test
    @DisplayName("타입 필터 조회 - 존재하지 않는 타입")
    void findClothsByCursor_FilterByNonExistentType() {
        // when
        ClothDTOCursorResponse response = clothQueryRepository.findClothsByCursor(
                owner1.getId(), null, null, 20, "createdAt", "desc", "SHOES"
        );

        // then
        assertThat(response.data()).isEmpty();
        assertThat(response.hasNext()).isFalse();
        assertThat(response.totalCount()).isEqualTo(4); // totalCount는 전체 owner1 옷 개수
    }

    @Test
    @DisplayName("이름으로 정렬 - ASCENDING")
    void findClothsByCursor_SortByName_Ascending() {
        // when
        ClothDTOCursorResponse response = clothQueryRepository.findClothsByCursor(
                owner1.getId(), null, null, 20, "name", "asc", null
        );

        // then
        assertThat(response.data()).hasSize(4);
        assertThat(response.sortBy()).isEqualTo("name");

        // 이름이 알파벳 순으로 정렬되어야 함
        List<String> names = response.data().stream().map(dto -> dto.name()).toList();
        for (int i = 0; i < names.size() - 1; i++) {
            assertThat(names.get(i).compareTo(names.get(i + 1))).isLessThanOrEqualTo(0);
        }

        // Black Jacket -> Blue Jeans -> Red Dress -> White T-Shirt
        assertThat(names.get(0)).isEqualTo("Black Jacket");
    }

    @Test
    @DisplayName("이름으로 정렬 - DESCENDING")
    void findClothsByCursor_SortByName_Descending() {
        // when
        ClothDTOCursorResponse response = clothQueryRepository.findClothsByCursor(
                owner1.getId(), null, null, 20, "name", "desc", null
        );

        // then
        assertThat(response.data()).hasSize(4);
        assertThat(response.sortBy()).isEqualTo("name");

        // 이름이 역순으로 정렬되어야 함
        List<String> names = response.data().stream().map(dto -> dto.name()).toList();
        for (int i = 0; i < names.size() - 1; i++) {
            assertThat(names.get(i).compareTo(names.get(i + 1))).isGreaterThanOrEqualTo(0);
        }

        // White T-Shirt -> Red Dress -> Blue Jeans -> Black Jacket
        assertThat(names.get(0)).isEqualTo("White T-Shirt");
    }

    @Test
    @DisplayName("페이지 크기 제한 테스트")
    void findClothsByCursor_LimitTest() {
        // when - limit 2 설정
        ClothDTOCursorResponse response = clothQueryRepository.findClothsByCursor(
                owner1.getId(), null, null, 2, "createdAt", "desc", null
        );

        // then
        assertThat(response.data()).hasSize(2);
        assertThat(response.hasNext()).isTrue();
    }

    @Test
    @DisplayName("기본 limit 값 테스트 (null 전달)")
    void findClothsByCursor_DefaultLimit() {
        // when - limit null (기본값 20)
        ClothDTOCursorResponse response = clothQueryRepository.findClothsByCursor(
                owner1.getId(), null, null, null, "createdAt", "desc", null
        );

        // then
        assertThat(response.data()).hasSize(4); // owner1의 옷 4개
        assertThat(response.hasNext()).isFalse();
    }

    @Test
    @DisplayName("마지막 페이지 조회 - hasNext false")
    void findClothsByCursor_LastPage() {
        // when - 충분히 큰 limit으로 모든 데이터 조회
        ClothDTOCursorResponse response = clothQueryRepository.findClothsByCursor(
                owner1.getId(), null, null, 100, "createdAt", "desc", null
        );

        // then
        assertThat(response.hasNext()).isFalse();
        assertThat(response.data().size()).isEqualTo((int) response.totalCount());
    }

    @Test
    @DisplayName("다른 소유자의 옷은 조회되지 않음")
    void findClothsByCursor_DifferentOwner() {
        // when - owner2의 옷 조회
        ClothDTOCursorResponse response = clothQueryRepository.findClothsByCursor(
                owner2.getId(), null, null, 20, "createdAt", "desc", null
        );

        // then
        assertThat(response.data()).hasSize(1);
        assertThat(response.data().get(0).name()).isEqualTo("Gray Sneakers");
        assertThat(response.totalCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("기본 정렬 옵션 테스트 (null sortBy, null sortDirection)")
    void findClothsByCursor_DefaultSorting() {
        // when
        ClothDTOCursorResponse response = clothQueryRepository.findClothsByCursor(
                owner1.getId(), null, null, 20, null, null, null
        );

        // then
        assertThat(response.sortBy()).isEqualTo("createdAt");
        assertThat(response.sortDirection()).isEqualTo("desc");
    }

    @Test
    @DisplayName("잘못된 커서 처리 - 유효하지 않은 Base64")
    void findClothsByCursor_InvalidCursor() {
        // when - 잘못된 커서 (유효하지 않은 Base64 또는 형식)
        ClothDTOCursorResponse response = clothQueryRepository.findClothsByCursor(
                owner1.getId(), "invalid-cursor-string", null, 20, "createdAt", "desc", null
        );

        // then - 커서를 무시하고 첫 페이지처럼 동작
        assertThat(response.data()).isNotEmpty();
        assertThat(response.data()).hasSize(4);
    }

    @Test
    @DisplayName("nextIdAfter 값 확인")
    void findClothsByCursor_NextIdAfter() {
        // when
        ClothDTOCursorResponse response = clothQueryRepository.findClothsByCursor(
                owner1.getId(), null, null, 3, "createdAt", "desc", null
        );

        // then
        if (response.hasNext()) {
            assertThat(response.nextIdAfter()).isNotNull();
            UUID lastClothId = response.data().get(response.data().size() - 1).id();
            assertThat(response.nextIdAfter()).isEqualTo(lastClothId);
        }
    }

    @Test
    @DisplayName("totalCount 정확성 검증")
    void findClothsByCursor_TotalCount() {
        // given
        long actualClothCount = clothRepository.findAllByOwner_Id(owner1.getId()).size();

        // when
        ClothDTOCursorResponse response = clothQueryRepository.findClothsByCursor(
                owner1.getId(), null, null, 20, "createdAt", "desc", null
        );

        // then
        assertThat(response.totalCount()).isEqualTo(actualClothCount);
        assertThat(response.totalCount()).isEqualTo(4);
    }

    @Test
    @DisplayName("빈 결과 조회 - 옷이 없는 소유자")
    void findClothsByCursor_EmptyResult() {
        // given - 옷이 없는 새 소유자
        User owner3 = User.builder()
                .email("owner3@example.com")
                .password("password123")
                .name("owner3")
                .build();
        userRepository.save(owner3);
        userRepository.flush();

        // when
        ClothDTOCursorResponse response = clothQueryRepository.findClothsByCursor(
                owner3.getId(), null, null, 20, "createdAt", "desc", null
        );

        // then
        assertThat(response.data()).isEmpty();
        assertThat(response.hasNext()).isFalse();
        assertThat(response.totalCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("DESCENDING 대소문자 무관 정렬")
    void findClothsByCursor_DescendingCaseInsensitive() {
        // when
        ClothDTOCursorResponse response1 = clothQueryRepository.findClothsByCursor(
                owner1.getId(), null, null, 3, "createdAt", "DESCENDING", null
        );

        ClothDTOCursorResponse response2 = clothQueryRepository.findClothsByCursor(
                owner1.getId(), null, null, 3, "createdAt", "desc", null
        );

        // then - 같은 결과
        assertThat(response1.data().get(0).id()).isEqualTo(response2.data().get(0).id());
        assertThat(response1.sortDirection()).isIn("desc", "DESCENDING");
    }

    @Test
    @DisplayName("여러 타입의 옷 필터링 테스트")
    void findClothsByCursor_MultipleTypes() {
        // when - BOTTOM 타입 조회
        ClothDTOCursorResponse bottomResponse = clothQueryRepository.findClothsByCursor(
                owner1.getId(), null, null, 20, "createdAt", "desc", "BOTTOM"
        );

        // when - DRESS 타입 조회
        ClothDTOCursorResponse dressResponse = clothQueryRepository.findClothsByCursor(
                owner1.getId(), null, null, 20, "createdAt", "desc", "DRESS"
        );

        // then
        assertThat(bottomResponse.data()).hasSize(1);
        assertThat(bottomResponse.data().get(0).type()).isEqualTo("BOTTOM");

        assertThat(dressResponse.data()).hasSize(1);
        assertThat(dressResponse.data().get(0).type()).isEqualTo("DRESS");
    }

    @Test
    @DisplayName("페이징 중 nextCursor가 제대로 생성되는지 확인")
    void findClothsByCursor_NextCursorGeneration() {
        // when - 페이지 크기를 1로 설정하여 순차적으로 조회
        ClothDTOCursorResponse page1 = clothQueryRepository.findClothsByCursor(
                owner1.getId(), null, null, 1, "createdAt", "desc", null
        );

        // then
        assertThat(page1.hasNext()).isTrue();
        assertThat(page1.nextCursor()).isNotNull();
        assertThat(page1.nextIdAfter()).isNotNull();

        // when - nextCursor로 다음 페이지 조회
        ClothDTOCursorResponse page2 = clothQueryRepository.findClothsByCursor(
                owner1.getId(), page1.nextCursor(), null, 1, "createdAt", "desc", null
        );

        // then - 다른 옷이 조회되어야 함
        assertThat(page2.data()).hasSize(1);
        assertThat(page2.data().get(0).id()).isNotEqualTo(page1.data().get(0).id());
    }
}
