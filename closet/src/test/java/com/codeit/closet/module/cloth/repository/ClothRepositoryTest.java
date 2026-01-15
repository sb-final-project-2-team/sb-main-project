package com.codeit.closet.module.cloth.repository;

import com.codeit.closet.module.cloth.entity.Cloth;
import com.codeit.closet.module.cloth.entity.ClothType;
import com.codeit.closet.module.user.entity.User;
import com.codeit.closet.module.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@DisplayName("ClothRepository 테스트")
class ClothRepositoryTest {

    @Autowired
    private ClothRepository clothRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    private User testUser;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        testUser = User.builder()
                .name("testuser")
                .email("test@example.com")
                .password("password123")
                .provider(com.codeit.closet.module.user.entity.AuthProvider.LOCAL)
                .providerId("test-provider-id")
                .role(com.codeit.closet.module.user.entity.UserRole.USER)
                .build();
        testUser = userRepository.save(testUser);
    }

    @Test
    @DisplayName("의상 저장 성공")
    void saveCloth_Success() {
        // given
        Cloth cloth = Cloth.builder()
                .owner(testUser)
                .name("블루 데님 자켓")
                .type(ClothType.OUTER)
                .build();

        // when
        Cloth saved = clothRepository.save(cloth);
        entityManager.flush();

        // then
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("블루 데님 자켓");
        assertThat(saved.getType()).isEqualTo(ClothType.OUTER);
        assertThat(saved.getOwner().getId()).isEqualTo(testUser.getId());
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("ID로 의상 조회 성공")
    void findById_Success() {
        // given
        Cloth cloth = Cloth.builder()
                .owner(testUser)
                .name("화이트 티셔츠")
                .type(ClothType.TOP)
                .build();
        Cloth saved = clothRepository.save(cloth);

        // when
        Optional<Cloth> found = clothRepository.findById(saved.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("화이트 티셔츠");
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회 시 빈 Optional 반환")
    void findById_NotFound() {
        // given
        UUID nonExistentId = UUID.randomUUID();

        // when
        Optional<Cloth> found = clothRepository.findById(nonExistentId);

        // then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("소유자 ID로 의상 목록 조회 성공")
    void findAllByOwner_Id_Success() {
        // given
        Cloth cloth1 = Cloth.builder()
                .owner(testUser)
                .name("블랙 자켓")
                .type(ClothType.OUTER)
                .build();
        Cloth cloth2 = Cloth.builder()
                .owner(testUser)
                .name("화이트 셔츠")
                .type(ClothType.TOP)
                .build();
        Cloth cloth3 = Cloth.builder()
                .owner(testUser)
                .name("블루 진")
                .type(ClothType.BOTTOM)
                .build();

        clothRepository.saveAll(List.of(cloth1, cloth2, cloth3));

        // when
        List<Cloth> clothList = clothRepository.findAllByOwner_Id(testUser.getId());

        // then
        assertThat(clothList).hasSize(3);
        assertThat(clothList)
                .extracting(Cloth::getName)
                .containsExactlyInAnyOrder("블랙 자켓", "화이트 셔츠", "블루 진");
    }

    @Test
    @DisplayName("소유자 ID로 조회 시 해당 소유자의 의상만 반환")
    void findAllByOwner_Id_OnlyOwnerClothes() {
        // given
        User anotherUser = User.builder()
                .name("anotheruser")
                .email("another@example.com")
                .password("password456")
                .provider(com.codeit.closet.module.user.entity.AuthProvider.LOCAL)
                .providerId("another-provider-id")
                .role(com.codeit.closet.module.user.entity.UserRole.USER)
                .build();
        anotherUser = userRepository.save(anotherUser);

        Cloth cloth1 = Cloth.builder()
                .owner(testUser)
                .name("내 자켓")
                .type(ClothType.OUTER)
                .build();
        Cloth cloth2 = Cloth.builder()
                .owner(anotherUser)
                .name("다른 사람 자켓")
                .type(ClothType.OUTER)
                .build();

        clothRepository.saveAll(List.of(cloth1, cloth2));

        // when
        List<Cloth> clothList = clothRepository.findAllByOwner_Id(testUser.getId());

        // then
        assertThat(clothList).hasSize(1);
        assertThat(clothList.get(0).getName()).isEqualTo("내 자켓");
    }

    @Test
    @DisplayName("소유자와 이름으로 중복 검사 - 존재하는 경우")
    void existsByOwner_IdAndName_Exists() {
        // given
        Cloth cloth = Cloth.builder()
                .owner(testUser)
                .name("블루 데님 자켓")
                .type(ClothType.OUTER)
                .build();
        clothRepository.save(cloth);

        // when
        boolean exists = clothRepository.existsByOwner_IdAndName(testUser.getId(), "블루 데님 자켓");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("소유자와 이름으로 중복 검사 - 존재하지 않는 경우")
    void existsByOwner_IdAndName_NotExists() {
        // given
        Cloth cloth = Cloth.builder()
                .owner(testUser)
                .name("블루 데님 자켓")
                .type(ClothType.OUTER)
                .build();
        clothRepository.save(cloth);

        // when
        boolean exists = clothRepository.existsByOwner_IdAndName(testUser.getId(), "레드 자켓");

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("다른 소유자가 같은 이름의 의상을 가진 경우 중복으로 판단하지 않음")
    void existsByOwner_IdAndName_DifferentOwner() {
        // given
        User anotherUser = User.builder()
                .name("anotheruser")
                .email("another@example.com")
                .password("password456")
                .provider(com.codeit.closet.module.user.entity.AuthProvider.LOCAL)
                .providerId("another-provider-id")
                .role(com.codeit.closet.module.user.entity.UserRole.USER)
                .build();
        anotherUser = userRepository.save(anotherUser);

        Cloth cloth = Cloth.builder()
                .owner(testUser)
                .name("블루 데님 자켓")
                .type(ClothType.OUTER)
                .build();
        clothRepository.save(cloth);

        // when
        boolean exists = clothRepository.existsByOwner_IdAndName(anotherUser.getId(), "블루 데님 자켓");

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("의상 삭제 성공")
    void deleteCloth_Success() {
        // given
        Cloth cloth = Cloth.builder()
                .owner(testUser)
                .name("블루 데님 자켓")
                .type(ClothType.OUTER)
                .build();
        Cloth saved = clothRepository.save(cloth);

        // when
        clothRepository.deleteById(saved.getId());

        // then
        Optional<Cloth> found = clothRepository.findById(saved.getId());
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("의상 업데이트 성공")
    void updateCloth_Success() {
        // given
        Cloth cloth = Cloth.builder()
                .owner(testUser)
                .name("블루 데님 자켓")
                .type(ClothType.OUTER)
                .build();
        Cloth saved = clothRepository.save(cloth);
        entityManager.flush();
        entityManager.clear();

        // when
        Cloth found = clothRepository.findById(saved.getId()).orElseThrow();
        found.updateName("다크 블루 데님 자켓");
        found.updateType(ClothType.DRESS);
        Cloth updated = clothRepository.save(found);
        entityManager.flush();

        // then
        assertThat(updated.getName()).isEqualTo("다크 블루 데님 자켓");
        assertThat(updated.getType()).isEqualTo(ClothType.DRESS);
        assertThat(updated.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("빈 목록 조회 시 빈 리스트 반환")
    void findAllByOwner_Id_EmptyList() {
        // given
        UUID nonExistentUserId = UUID.randomUUID();

        // when
        List<Cloth> clothList = clothRepository.findAllByOwner_Id(nonExistentUserId);

        // then
        assertThat(clothList).isEmpty();
    }
}
