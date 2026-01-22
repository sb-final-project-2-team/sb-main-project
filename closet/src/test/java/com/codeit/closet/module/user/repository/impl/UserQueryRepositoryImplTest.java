package com.codeit.closet.module.user.repository.impl;

import com.codeit.closet.module.user.dto.user.UserDTOCursorResponse;
import com.codeit.closet.module.user.entity.User;
import com.codeit.closet.module.user.entity.UserRole;
import com.codeit.closet.module.user.repository.UserQueryRepository;
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
@DisplayName("UserQueryRepositoryImpl 통합 테스트")
class UserQueryRepositoryImplTest {

    @Autowired
    @Qualifier("userQueryRepositoryImpl")
    private UserQueryRepository userQueryRepository;

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;
    private User user3;
    private User adminUser;
    private User lockedUser;

    @BeforeEach
    void setUp() throws InterruptedException {
        // 다양한 테스트 데이터 생성
        user1 = User.builder()
                .email("alice@example.com")
                .password("password123")
                .name("alice")
                .build();
        userRepository.save(user1);
        userRepository.flush();

        // 시간차를 두기 위한 짧은 대기
        Thread.sleep(10);

        user2 = User.builder()
                .email("bob@example.com")
                .password("password123")
                .name("bob")
                .build();
        userRepository.save(user2);
        userRepository.flush();

        Thread.sleep(10);

        user3 = User.builder()
                .email("charlie@example.com")
                .password("password123")
                .name("charlie")
                .build();
        userRepository.save(user3);
        userRepository.flush();

        Thread.sleep(10);

        adminUser = User.builder()
                .email("admin@example.com")
                .password("password123")
                .name("admin")
                .build();
        adminUser.updateRole(UserRole.ADMIN);
        userRepository.save(adminUser);
        userRepository.flush();

        Thread.sleep(10);

        lockedUser = User.builder()
                .email("locked@example.com")
                .password("password123")
                .name("locked")
                .build();
        lockedUser.updateLocked(true);
        userRepository.save(lockedUser);
        userRepository.flush();
    }

    @Test
    @DisplayName("커서 없이 기본 페이징 조회 - DESCENDING 정렬")
    void findUsersByCursor_WithoutCursor_Descending() {
        // when
        UserDTOCursorResponse response = userQueryRepository.findUsersByCursor(
                null, null, 3, "createdAt", "DESCENDING", null, null, null
        );

        // then
        assertThat(response.data()).hasSize(3);
        assertThat(response.hasNext()).isTrue();
        assertThat(response.nextCursor()).isNotNull();
        assertThat(response.totalCount()).isGreaterThanOrEqualTo(5L);
        assertThat(response.sortBy()).isEqualTo("createdAt");
        assertThat(response.sortDirection()).isEqualTo("DESCENDING");

        // 최신 사용자가 먼저 나와야 함
        assertThat(response.data().get(0).email()).isEqualTo("locked@example.com");
    }

    @Test
    @DisplayName("커서 없이 기본 페이징 조회 - ASCENDING 정렬")
    void findUsersByCursor_WithoutCursor_Ascending() {
        // when
        UserDTOCursorResponse response = userQueryRepository.findUsersByCursor(
                null, null, 3, "createdAt", "ASCENDING", null, null, null
        );

        // then
        assertThat(response.data()).hasSize(3);
        assertThat(response.hasNext()).isTrue();
        assertThat(response.sortDirection()).isEqualTo("ASCENDING");

        // createdAt 기준 오름차순 정렬 확인
        assertThat(response.data().get(0).createdAt())
                .isBeforeOrEqualTo(response.data().get(1).createdAt());
        assertThat(response.data().get(1).createdAt())
                .isBeforeOrEqualTo(response.data().get(2).createdAt());
    }

    @Test
    @DisplayName("커서를 사용한 다음 페이지 조회")
    void findUsersByCursor_WithCursor() {
        // given - 첫 페이지 조회
        UserDTOCursorResponse firstPage = userQueryRepository.findUsersByCursor(
                null, null, 2, "createdAt", "DESCENDING", null, null, null
        );

        assertThat(firstPage.data()).hasSize(2);
        assertThat(firstPage.hasNext()).isTrue();

        String cursor = firstPage.nextCursor();

        // when - 두 번째 페이지 조회
        UserDTOCursorResponse secondPage = userQueryRepository.findUsersByCursor(
                cursor, null, 2, "createdAt", "DESCENDING", null, null, null
        );

        // then
        assertThat(secondPage.data()).hasSizeGreaterThan(0);
        assertThat(secondPage.data().get(0).id()).isNotEqualTo(firstPage.data().get(0).id());
        assertThat(secondPage.data().get(0).id()).isNotEqualTo(firstPage.data().get(1).id());
    }

    @Test
    @DisplayName("이메일 필터 조회 - 부분 일치")
    void findUsersByCursor_FilterByEmailLike() {
        // when - "alice" 포함 검색
        UserDTOCursorResponse response = userQueryRepository.findUsersByCursor(
                null, null, 20, "createdAt", "DESCENDING", "alice", null, null
        );

        // then
        assertThat(response.data()).isNotEmpty();
        assertThat(response.data()).allMatch(dto -> dto.email().toLowerCase().contains("alice"));
        assertThat(response.totalCount()).isGreaterThanOrEqualTo(1L);
    }

    @Test
    @DisplayName("역할 필터 조회 - ADMIN")
    void findUsersByCursor_FilterByRole() {
        // when
        UserDTOCursorResponse response = userQueryRepository.findUsersByCursor(
                null, null, 20, "createdAt", "DESCENDING", null, "ADMIN", null
        );

        // then
        assertThat(response.data()).isNotEmpty();
        assertThat(response.data()).allMatch(dto -> dto.role() == UserRole.ADMIN);
        assertThat(response.data().get(0).email()).isEqualTo("admin@example.com");
    }

    @Test
    @DisplayName("역할 필터 조회 - USER")
    void findUsersByCursor_FilterByUserRole() {
        // when
        UserDTOCursorResponse response = userQueryRepository.findUsersByCursor(
                null, null, 20, "createdAt", "DESCENDING", null, "USER", null
        );

        // then
        assertThat(response.data()).isNotEmpty();
        assertThat(response.data()).allMatch(dto -> dto.role() == UserRole.USER);
        assertThat(response.totalCount()).isGreaterThanOrEqualTo(4L); // alice, bob, charlie, locked
    }

    @Test
    @DisplayName("잠금 상태 필터 조회 - 잠긴 사용자만")
    void findUsersByCursor_FilterByLocked_True() {
        // when
        UserDTOCursorResponse response = userQueryRepository.findUsersByCursor(
                null, null, 20, "createdAt", "DESCENDING", null, null, true
        );

        // then
        assertThat(response.data()).isNotEmpty();
        assertThat(response.data()).allMatch(dto -> dto.locked());
        assertThat(response.data().get(0).email()).isEqualTo("locked@example.com");
    }

    @Test
    @DisplayName("잠금 상태 필터 조회 - 잠기지 않은 사용자만")
    void findUsersByCursor_FilterByLocked_False() {
        // when
        UserDTOCursorResponse response = userQueryRepository.findUsersByCursor(
                null, null, 20, "createdAt", "DESCENDING", null, null, false
        );

        // then
        assertThat(response.data()).isNotEmpty();
        assertThat(response.data()).allMatch(dto -> !dto.locked());
        assertThat(response.totalCount()).isGreaterThanOrEqualTo(4L);
    }

    @Test
    @DisplayName("복합 필터 조회 - 이메일 + 역할")
    void findUsersByCursor_MultipleFilters() {
        // when - "example.com" 포함 + USER 역할
        UserDTOCursorResponse response = userQueryRepository.findUsersByCursor(
                null, null, 20, "createdAt", "DESCENDING", "example", "USER", null
        );

        // then
        assertThat(response.data()).isNotEmpty();
        assertThat(response.data()).allMatch(dto ->
            dto.email().contains("example") && dto.role() == UserRole.USER
        );
    }

    @Test
    @DisplayName("복합 필터 조회 - 이메일 + 잠금 상태")
    void findUsersByCursor_EmailAndLocked() {
        // when
        UserDTOCursorResponse response = userQueryRepository.findUsersByCursor(
                null, null, 20, "createdAt", "DESCENDING", "locked", null, true
        );

        // then
        assertThat(response.data()).hasSize(1);
        assertThat(response.data().get(0).email()).isEqualTo("locked@example.com");
        assertThat(response.data().get(0).locked()).isTrue();
    }

    @Test
    @DisplayName("이메일로 정렬 - ASCENDING")
    void findUsersByCursor_SortByEmail_Ascending() {
        // when
        UserDTOCursorResponse response = userQueryRepository.findUsersByCursor(
                null, null, 20, "email", "ASCENDING", null, null, null
        );

        // then
        assertThat(response.data()).isNotEmpty();
        assertThat(response.sortBy()).isEqualTo("email");

        // 이메일이 알파벳 순으로 정렬되어야 함
        List<String> emails = response.data().stream().map(dto -> dto.email()).toList();
        for (int i = 0; i < emails.size() - 1; i++) {
            assertThat(emails.get(i).compareTo(emails.get(i + 1))).isLessThanOrEqualTo(0);
        }
    }

    @Test
    @DisplayName("이메일로 정렬 - DESCENDING")
    void findUsersByCursor_SortByEmail_Descending() {
        // when
        UserDTOCursorResponse response = userQueryRepository.findUsersByCursor(
                null, null, 20, "email", "DESCENDING", null, null, null
        );

        // then
        assertThat(response.data()).isNotEmpty();
        assertThat(response.sortBy()).isEqualTo("email");

        // 이메일이 역순으로 정렬되어야 함
        List<String> emails = response.data().stream().map(dto -> dto.email()).toList();
        for (int i = 0; i < emails.size() - 1; i++) {
            assertThat(emails.get(i).compareTo(emails.get(i + 1))).isGreaterThanOrEqualTo(0);
        }
    }

    @Test
    @DisplayName("페이지 크기 제한 테스트")
    void findUsersByCursor_LimitTest() {
        // when - limit 2 설정
        UserDTOCursorResponse response = userQueryRepository.findUsersByCursor(
                null, null, 2, "createdAt", "DESCENDING", null, null, null
        );

        // then
        assertThat(response.data()).hasSize(2);
        assertThat(response.hasNext()).isTrue();
    }

    @Test
    @DisplayName("기본 limit 값 테스트 (null 전달)")
    void findUsersByCursor_DefaultLimit() {
        // when - limit null (기본값 20)
        UserDTOCursorResponse response = userQueryRepository.findUsersByCursor(
                null, null, null, "createdAt", "DESCENDING", null, null, null
        );

        // then
        assertThat(response.data()).hasSizeGreaterThan(0);
        assertThat(response.data()).hasSizeLessThanOrEqualTo(20);
    }

    @Test
    @DisplayName("마지막 페이지 조회 - hasNext false")
    void findUsersByCursor_LastPage() {
        // when - 충분히 큰 limit으로 모든 데이터 조회
        UserDTOCursorResponse response = userQueryRepository.findUsersByCursor(
                null, null, 100, "createdAt", "DESCENDING", null, null, null
        );

        // then
        assertThat(response.hasNext()).isFalse();
        assertThat(response.data().size()).isEqualTo(response.totalCount().intValue());
    }

    @Test
    @DisplayName("필터 결과 없음")
    void findUsersByCursor_NoResults() {
        // when - 존재하지 않는 이메일 검색
        UserDTOCursorResponse response = userQueryRepository.findUsersByCursor(
                null, null, 20, "createdAt", "DESCENDING", "nonexistent@nowhere.com", null, null
        );

        // then
        assertThat(response.data()).isEmpty();
        assertThat(response.hasNext()).isFalse();
        assertThat(response.totalCount()).isEqualTo(0L);
    }

    @Test
    @DisplayName("기본 정렬 옵션 테스트 (null sortBy, null sortDirection)")
    void findUsersByCursor_DefaultSorting() {
        // when
        UserDTOCursorResponse response = userQueryRepository.findUsersByCursor(
                null, null, 20, null, null, null, null, null
        );

        // then
        assertThat(response.sortBy()).isEqualTo("createdAt");
        assertThat(response.sortDirection()).isEqualTo("DESCENDING");
    }

    @Test
    @DisplayName("잘못된 커서 처리 - 유효하지 않은 Base64")
    void findUsersByCursor_InvalidCursor() {
        // when - 잘못된 커서 (유효하지 않은 Base64 또는 형식)
        UserDTOCursorResponse response = userQueryRepository.findUsersByCursor(
                "invalid-cursor-string", null, 20, "createdAt", "DESCENDING", null, null, null
        );

        // then - 커서를 무시하고 첫 페이지처럼 동작
        assertThat(response.data()).isNotEmpty();
    }

    @Test
    @DisplayName("nextAfter 값 확인")
    void findUsersByCursor_NextAfter() {
        // when
        UserDTOCursorResponse response = userQueryRepository.findUsersByCursor(
                null, null, 3, "createdAt", "DESCENDING", null, null, null
        );

        // then
        if (response.hasNext()) {
            assertThat(response.nextAfter()).isNotNull();
            UUID lastUserId = response.data().get(response.data().size() - 1).id();
            assertThat(response.nextAfter()).isEqualTo(lastUserId);
        }
    }

    @Test
    @DisplayName("totalCount 정확성 검증")
    void findUsersByCursor_TotalCount() {
        // given
        long actualUserCount = userRepository.count();

        // when
        UserDTOCursorResponse response = userQueryRepository.findUsersByCursor(
                null, null, 20, "createdAt", "DESCENDING", null, null, null
        );

        // then
        assertThat(response.totalCount()).isEqualTo(actualUserCount);
    }

    @Test
    @DisplayName("필터 적용 시 totalCount 정확성")
    void findUsersByCursor_TotalCount_WithFilter() {
        // when
        UserDTOCursorResponse response = userQueryRepository.findUsersByCursor(
                null, null, 20, "createdAt", "DESCENDING", null, "ADMIN", null
        );

        // then
        long adminCount = response.data().size();
        assertThat(response.totalCount()).isEqualTo(adminCount);
    }
}
