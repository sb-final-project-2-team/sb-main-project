package com.codeit.closet.module.user.mapper;

import com.codeit.closet.module.binarycontent.entity.BinaryContent;
import com.codeit.closet.module.user.dto.profile.ProfileDTO;
import com.codeit.closet.module.user.dto.user.UserDTO;
import com.codeit.closet.module.user.dto.user.UserSummary;
import com.codeit.closet.module.user.entity.AuthProvider;
import com.codeit.closet.module.user.entity.User;
import com.codeit.closet.module.user.entity.UserGender;
import com.codeit.closet.module.user.entity.UserRole;
import com.codeit.closet.module.weather.entity.WeatherRegion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("UserMapper 통합 테스트")
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    @DisplayName("toProfileDTO - BinaryContent와 Weather가 모두 있는 경우")
    void toProfileDTO_WithBinaryContentAndWeather() {
        // given
        BinaryContent binaryContent = BinaryContent.builder()
                .fileUrl("https://example.com/profile.jpg")
                .fileName("profile.jpg")
                .size(1024L)
                .contentType("image/jpeg")
                .build();

        WeatherRegion weatherRegion = WeatherRegion.builder()
                .latitude(37.5665)
                .longitude(126.9780)
                .x(60)
                .y(127)
                .locationNames("서울특별시,중구,명동")
                .build();

        User user = User.builder()
                .name("홍길동")
                .email("hong@example.com")
                .password("password123")
                .gender(UserGender.MALE)
                .birthDate(Instant.parse("1990-01-01T00:00:00Z"))
                .temperatureSensitivity(3)
                .role(UserRole.USER)
                .provider(AuthProvider.LOCAL)
                .providerId("local-123")
                .binaryContent(binaryContent)
                .weather(weatherRegion)
                .build();

        // when
        ProfileDTO result = userMapper.toProfileDTO(user);

        // then
        assertThat(result).isNotNull();
        assertThat(result.userId()).isNull(); // ID는 빌더로 설정하지 않아서 null
        assertThat(result.name()).isEqualTo("홍길동");
        assertThat(result.gender()).isEqualTo(UserGender.MALE);
        assertThat(result.birthDate()).isEqualTo(Instant.parse("1990-01-01T00:00:00Z"));
        assertThat(result.temperatureSensitivity()).isEqualTo(3);
        assertThat(result.profileImageUrl()).isEqualTo("https://example.com/profile.jpg");

        // WeatherRegion -> WeatherAPILocation 변환 검증
        assertThat(result.location()).isNotNull();
        assertThat(result.location().latitude()).isEqualTo(37.5665);
        assertThat(result.location().longitude()).isEqualTo(126.9780);
        assertThat(result.location().x()).isEqualTo(60);
        assertThat(result.location().y()).isEqualTo(127);
        assertThat(result.location().locationNames()).containsExactly("서울특별시", "중구", "명동");
    }

    @Test
    @DisplayName("toProfileDTO - BinaryContent가 null인 경우")
    void toProfileDTO_WithoutBinaryContent() {
        // given
        User user = User.builder()
                .name("김철수")
                .email("kim@example.com")
                .password("password123")
                .gender(UserGender.MALE)
                .temperatureSensitivity(5)
                .role(UserRole.USER)
                .provider(AuthProvider.LOCAL)
                .providerId("local-456")
                .binaryContent(null)
                .weather(null)
                .build();

        // when
        ProfileDTO result = userMapper.toProfileDTO(user);

        // then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("김철수");
        assertThat(result.profileImageUrl()).isNull();
        assertThat(result.location()).isNull();
    }

    @Test
    @DisplayName("toProfileDTO - Weather만 있는 경우")
    void toProfileDTO_WithOnlyWeather() {
        // given
        WeatherRegion weatherRegion = WeatherRegion.builder()
                .latitude(35.1796)
                .longitude(129.0756)
                .x(98)
                .y(76)
                .locationNames("부산광역시,해운대구")
                .build();

        User user = User.builder()
                .name("이영희")
                .email("lee@example.com")
                .password("password123")
                .gender(UserGender.FEMALE)
                .birthDate(Instant.parse("1995-06-15T00:00:00Z"))
                .temperatureSensitivity(2)
                .role(UserRole.USER)
                .provider(AuthProvider.GOOGLE)
                .providerId("google-789")
                .binaryContent(null)
                .weather(weatherRegion)
                .build();

        // when
        ProfileDTO result = userMapper.toProfileDTO(user);

        // then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("이영희");
        assertThat(result.gender()).isEqualTo(UserGender.FEMALE);
        assertThat(result.profileImageUrl()).isNull();
        assertThat(result.location()).isNotNull();
        assertThat(result.location().locationNames()).containsExactly("부산광역시", "해운대구");
    }

    @Test
    @DisplayName("toProfileDTO - Gender가 null인 경우")
    void toProfileDTO_WithNullGender() {
        // given
        User user = User.builder()
                .name("박민수")
                .email("park@example.com")
                .password("password123")
                .gender(null)
                .temperatureSensitivity(4)
                .role(UserRole.USER)
                .provider(AuthProvider.LOCAL)
                .providerId("local-000")
                .build();

        // when
        ProfileDTO result = userMapper.toProfileDTO(user);

        // then
        assertThat(result).isNotNull();
        assertThat(result.gender()).isNull();
    }

    @Test
    @DisplayName("toUserDTO - 기본 변환 테스트")
    void toUserDTO_Success() {
        // given
        BinaryContent binaryContent = BinaryContent.builder()
                .fileUrl("https://example.com/avatar.jpg")
                .fileName("avatar.jpg")
                .size(2048L)
                .contentType("image/jpeg")
                .build();

        User user = User.builder()
                .name("최수진")
                .email("choi@example.com")
                .password("password123")
                .role(UserRole.ADMIN)
                .provider(AuthProvider.LOCAL)
                .providerId("local-111")
                .locked(false)
                .binaryContent(binaryContent)
                .build();

        // when
        UserDTO result = userMapper.toUserDTO(user);

        // then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("최수진");
        assertThat(result.email()).isEqualTo("choi@example.com");
        assertThat(result.role()).isEqualTo(UserRole.ADMIN);
        assertThat(result.locked()).isFalse();
    }

    @Test
    @DisplayName("toUserDTOs - 리스트 변환 테스트")
    void toUserDTOs_Success() {
        // given
        User user1 = User.builder()
                .name("사용자1")
                .email("user1@example.com")
                .password("password123")
                .role(UserRole.USER)
                .provider(AuthProvider.LOCAL)
                .providerId("local-001")
                .build();

        User user2 = User.builder()
                .name("사용자2")
                .email("user2@example.com")
                .password("password123")
                .role(UserRole.ADMIN)
                .provider(AuthProvider.GOOGLE)
                .providerId("google-002")
                .build();

        List<User> users = List.of(user1, user2);

        // when
        List<UserDTO> results = userMapper.toUserDTOs(users);

        // then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).name()).isEqualTo("사용자1");
        assertThat(results.get(0).role()).isEqualTo(UserRole.USER);
        assertThat(results.get(1).name()).isEqualTo("사용자2");
        assertThat(results.get(1).role()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    @DisplayName("toUserDTOs - 빈 리스트 처리")
    void toUserDTOs_EmptyList() {
        // given
        List<User> users = List.of();

        // when
        List<UserDTO> results = userMapper.toUserDTOs(users);

        // then
        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("toUserSummary - BinaryContent가 있는 경우")
    void toUserSummary_WithBinaryContent() {
        // given
        BinaryContent binaryContent = BinaryContent.builder()
                .fileUrl("https://example.com/summary.jpg")
                .fileName("summary.jpg")
                .size(512L)
                .contentType("image/jpeg")
                .build();

        User user = User.builder()
                .name("정지훈")
                .email("jung@example.com")
                .password("password123")
                .role(UserRole.USER)
                .provider(AuthProvider.LOCAL)
                .providerId("local-222")
                .binaryContent(binaryContent)
                .build();

        // when
        UserSummary result = userMapper.toUserSummary(user);

        // then
        assertThat(result).isNotNull();
        assertThat(result.userId()).isNull(); // ID는 빌더로 설정하지 않아서 null
        assertThat(result.name()).isEqualTo("정지훈");
        assertThat(result.profileImageUrl()).isEqualTo("https://example.com/summary.jpg");
    }

    @Test
    @DisplayName("toUserSummary - BinaryContent가 null인 경우")
    void toUserSummary_WithoutBinaryContent() {
        // given
        User user = User.builder()
                .name("강민호")
                .email("kang@example.com")
                .password("password123")
                .role(UserRole.USER)
                .provider(AuthProvider.LOCAL)
                .providerId("local-333")
                .binaryContent(null)
                .build();

        // when
        UserSummary result = userMapper.toUserSummary(user);

        // then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("강민호");
        assertThat(result.profileImageUrl()).isNull();
    }

    @Test
    @DisplayName("toProfileDTO - Weather locationNames가 공백인 경우")
    void toProfileDTO_WithEmptyLocationNames() {
        // given
        WeatherRegion weatherRegion = WeatherRegion.builder()
                .latitude(37.5)
                .longitude(127.0)
                .x(60)
                .y(120)
                .locationNames("")
                .build();

        User user = User.builder()
                .name("테스트유저")
                .email("test@example.com")
                .password("password123")
                .role(UserRole.USER)
                .provider(AuthProvider.LOCAL)
                .providerId("local-444")
                .weather(weatherRegion)
                .build();

        // when
        ProfileDTO result = userMapper.toProfileDTO(user);

        // then
        assertThat(result).isNotNull();
        assertThat(result.location()).isNotNull();
        assertThat(result.location().locationNames()).isEmpty();
    }
}
