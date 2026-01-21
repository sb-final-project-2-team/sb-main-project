package com.codeit.closet.module.user.repository;

import com.codeit.closet.module.user.entity.AuthProvider;
import com.codeit.closet.module.user.entity.User;
import com.codeit.closet.module.user.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DisplayName("UserRepository 테스트")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("test@example.com")
                .password("password123")
                .name("testuser")
                .build();
    }

    @Test
    @DisplayName("사용자 저장 성공")
    void save_Success() {
        // when
        User savedUser = userRepository.save(testUser);
        userRepository.flush();

        // then
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.getName()).isEqualTo("testuser");
        assertThat(savedUser.getRole()).isEqualTo(UserRole.USER);
        assertThat(savedUser.getLocked()).isFalse();
        assertThat(savedUser.getProvider()).isEqualTo(AuthProvider.LOCAL);
        assertThat(savedUser.getTemperatureSensitivity()).isEqualTo(3);
        assertThat(savedUser.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("ID로 사용자 조회 성공")
    void findById_Success() {
        // given
        User savedUser = userRepository.save(testUser);

        // when
        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getId()).isEqualTo(savedUser.getId());
    }

    @Test
    @DisplayName("이메일로 사용자 조회 성공")
    void findByEmail_Success() {
        // given
        userRepository.save(testUser);

        // when
        Optional<User> foundUser = userRepository.findByEmail("test@example.com");

        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("이메일로 사용자 조회 실패 - 존재하지 않는 이메일")
    void findByEmail_NotFound() {
        // when
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");

        // then
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("이메일 존재 여부 확인 - 존재하는 경우")
    void existsByEmail_True() {
        // given
        userRepository.save(testUser);

        // when
        boolean exists = userRepository.existsByEmail("test@example.com");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("이메일 존재 여부 확인 - 존재하지 않는 경우")
    void existsByEmail_False() {
        // when
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("이름 존재 여부 확인 - 존재하는 경우")
    void existsByName_True() {
        // given
        userRepository.save(testUser);

        // when
        boolean exists = userRepository.existsByName("testuser");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("이름 존재 여부 확인 - 존재하지 않는 경우")
    void existsByName_False() {
        // when
        boolean exists = userRepository.existsByName("nonexistentuser");

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Provider ID로 사용자 조회 성공")
    void findByProviderId_Success() {
        // given
        User savedUser = userRepository.save(testUser);

        // when
        Optional<User> foundUser = userRepository.findByProviderId(savedUser.getProviderId());

        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getProviderId()).isEqualTo(savedUser.getProviderId());
    }

    @Test
    @DisplayName("임시 비밀번호 설정 및 초기화")
    void clearTempPassword_Success() {
        // given
        testUser = userRepository.save(testUser);
        testUser.updateTempPassword("tempPassword123");
        userRepository.save(testUser);
        userRepository.flush();

        // when
        userRepository.clearTempPassword(testUser.getId());
        userRepository.flush();

        // then
        Optional<User> foundUser = userRepository.findById(testUser.getId());
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getTempPassword()).isNull();
        assertThat(foundUser.get().getTempPasswordExpiredAt()).isNull();
    }

    @Test
    @DisplayName("사용자 역할 업데이트")
    void updateRole_Success() {
        // given
        User savedUser = userRepository.save(testUser);

        // when
        savedUser.updateRole(UserRole.ADMIN);
        userRepository.save(savedUser);
        userRepository.flush();

        // then
        Optional<User> foundUser = userRepository.findById(savedUser.getId());
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    @DisplayName("사용자 잠금 상태 업데이트")
    void updateLocked_Success() {
        // given
        User savedUser = userRepository.save(testUser);

        // when
        savedUser.updateLocked(true);
        userRepository.save(savedUser);
        userRepository.flush();

        // then
        Optional<User> foundUser = userRepository.findById(savedUser.getId());
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getLocked()).isTrue();
    }

    @Test
    @DisplayName("사용자 비밀번호 변경")
    void changePassword_Success() {
        // given
        User savedUser = userRepository.save(testUser);

        // when
        savedUser.changePassword("newEncodedPassword");
        userRepository.save(savedUser);
        userRepository.flush();

        // then
        Optional<User> foundUser = userRepository.findById(savedUser.getId());
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getPassword()).isEqualTo("newEncodedPassword");
    }

    @Test
    @DisplayName("모든 사용자 조회")
    void findAll_Success() {
        // given
        User user1 = User.builder()
                .email("user1@example.com")
                .password("password123")
                .name("user1")
                .build();
        User user2 = User.builder()
                .email("user2@example.com")
                .password("password123")
                .name("user2")
                .build();

        userRepository.saveAll(List.of(testUser, user1, user2));

        // when
        List<User> allUsers = userRepository.findAll();

        // then
        assertThat(allUsers).hasSizeGreaterThanOrEqualTo(3);
    }

    @Test
    @DisplayName("사용자 개수 조회")
    void count_Success() {
        // given
        long beforeCount = userRepository.count();
        userRepository.save(testUser);

        // when
        long afterCount = userRepository.count();

        // then
        assertThat(afterCount).isEqualTo(beforeCount + 1);
    }

    @Test
    @DisplayName("사용자 삭제 성공")
    void deleteById_Success() {
        // given
        User savedUser = userRepository.save(testUser);

        // when
        userRepository.deleteById(savedUser.getId());
        userRepository.flush();

        // then
        Optional<User> foundUser = userRepository.findById(savedUser.getId());
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("사용자 존재 여부 확인 - 존재하는 경우")
    void existsById_True() {
        // given
        User savedUser = userRepository.save(testUser);

        // when
        boolean exists = userRepository.existsById(savedUser.getId());

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("사용자 존재 여부 확인 - 존재하지 않는 경우")
    void existsById_False() {
        // given
        UUID nonExistentId = UUID.randomUUID();

        // when
        boolean exists = userRepository.existsById(nonExistentId);

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("OAuth 사용자 생성")
    void saveOAuthUser_Success() {
        // given
        User oauthUser = User.builder()
                .email("oauth@example.com")
                .password("N/A")
                .name("oauthuser")
                .build();
        oauthUser.updateSocialInfo(AuthProvider.GOOGLE, "google-provider-id-123");

        // when
        User savedUser = userRepository.save(oauthUser);
        userRepository.flush();

        // then
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getProvider()).isEqualTo(AuthProvider.GOOGLE);
        assertThat(savedUser.getProviderId()).isEqualTo("google-provider-id-123");
    }
}
