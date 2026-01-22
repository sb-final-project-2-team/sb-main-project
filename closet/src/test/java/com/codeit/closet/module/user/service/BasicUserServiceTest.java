package com.codeit.closet.module.user.service;

import com.codeit.closet.common.exception.user.DuplicateUserException;
import com.codeit.closet.common.exception.user.UserNotFoundException;
import com.codeit.closet.module.binarycontent.entity.BinaryContent;
import com.codeit.closet.module.binarycontent.service.BinaryContentService;
import com.codeit.closet.module.user.dto.profile.ProfileDTO;
import com.codeit.closet.module.user.dto.profile.ProfileUpdateRequest;
import com.codeit.closet.module.user.dto.user.ChangePasswordRequest;
import com.codeit.closet.module.user.dto.user.UserCreateRequest;
import com.codeit.closet.module.user.dto.user.UserDTO;
import com.codeit.closet.module.user.dto.user.UserDTOCursorResponse;
import com.codeit.closet.module.user.dto.user.UserLockUpdateRequest;
import com.codeit.closet.module.user.dto.user.UserRoleUpdateRequest;
import com.codeit.closet.module.user.entity.User;
import com.codeit.closet.module.user.entity.UserRole;
import com.codeit.closet.module.user.mapper.UserMapper;
import com.codeit.closet.module.user.repository.UserRepository;
import com.codeit.closet.module.user.service.impl.BasicUserService;
import com.codeit.closet.module.weather.dto.location.WeatherAPILocation;
import com.codeit.closet.module.weather.entity.WeatherRegion;
import com.codeit.closet.module.weather.service.WeatherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BasicUserService 테스트")
class BasicUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private BinaryContentService binaryContentService;

    @Mock
    private WeatherService weatherService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private BasicUserService userService;

    private UUID testUserId;
    private User testUser;
    private UserDTO testUserDTO;
    private ProfileDTO testProfileDTO;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();

        testUser = User.builder()
                .id(testUserId)
                .email("test@example.com")
                .password("encodedPassword")
                .name("testuser")
                .role(UserRole.USER)
                .build();

        testUserDTO = new UserDTO(
                testUserId,
                java.time.Instant.now(),
                "test@example.com",
                "testuser",
                UserRole.USER,
                false
        );

        testProfileDTO = new ProfileDTO(
                testUserId,
                "testuser",
                null,
                null,
                null,
                null,
                null
        );
    }

    @Test
    @DisplayName("사용자 생성 성공")
    void createUser_Success() {
        // given
        UserCreateRequest request = new UserCreateRequest(
                "testuser",
                "password123",
                "test@example.com"
        );

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toUserDTO(any(User.class))).thenReturn(testUserDTO);

        // when
        UserDTO result = userService.createUser(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo("test@example.com");
        verify(userRepository, times(1)).existsByEmail(request.email());
        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, times(1)).encode(request.password());
    }

    @Test
    @DisplayName("사용자 생성 실패 - 중복 이메일")
    void createUser_Fail_DuplicateEmail() {
        // given
        UserCreateRequest request = new UserCreateRequest(
                "testuser",
                "password123",
                "test@example.com"
        );

        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(DuplicateUserException.class);

        verify(userRepository, times(1)).existsByEmail(request.email());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("사용자 역할 업데이트 성공")
    void updateUserRole_Success() {
        // given
        UserRoleUpdateRequest request = new UserRoleUpdateRequest(UserRole.ADMIN);

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userMapper.toUserDTO(any(User.class))).thenReturn(testUserDTO);

        // when
        UserDTO result = userService.updateUserRole(testUserId, request);

        // then
        assertThat(result).isNotNull();
        verify(userRepository, times(1)).findById(testUserId);

        verify(eventPublisher, times(1)).publishEvent(any(Object.class));
    }

    @Test
    @DisplayName("사용자 역할 업데이트 실패 - 사용자 없음")
    void updateUserRole_Fail_UserNotFound() {
        // given
        UserRoleUpdateRequest request = new UserRoleUpdateRequest(UserRole.ADMIN);

        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.updateUserRole(testUserId, request))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, times(1)).findById(testUserId);
    }

    @Test
    @DisplayName("사용자 프로필 조회 성공")
    void findUserProfile_Success() {
        // given
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userMapper.toProfileDTO(any(User.class))).thenReturn(testProfileDTO);

        // when
        ProfileDTO result = userService.findUserProfile(testUserId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(testUserId);
        verify(userRepository, times(1)).findById(testUserId);
    }

    @Test
    @DisplayName("비밀번호 변경 성공")
    void updateUserPassword_Success() {
        // given
        ChangePasswordRequest request = new ChangePasswordRequest("newPassword123");

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(request.password())).thenReturn("encodedNewPassword");

        // when
        userService.updateUserPassword(testUserId, request);

        // then
        verify(userRepository, times(1)).findById(testUserId);
        verify(passwordEncoder, times(1)).encode(request.password());
    }

    @Test
    @DisplayName("사용자 잠금 업데이트 성공")
    void updateUserLock_Success() {
        // given
        UserLockUpdateRequest request = new UserLockUpdateRequest(true);

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userMapper.toUserDTO(any(User.class))).thenReturn(testUserDTO);

        // when
        UserDTO result = userService.updateUserLock(testUserId, request);

        // then
        assertThat(result).isNotNull();
        verify(userRepository, times(1)).findById(testUserId);
    }

    @Test
    @DisplayName("사용자 프로필 조회 실패 - 사용자 없음")
    void findUserProfile_Fail_UserNotFound() {
        // given
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.findUserProfile(testUserId))
                .isInstanceOf(java.util.NoSuchElementException.class)
                .hasMessageContaining("존재하지 않는 회원입니다");

        verify(userRepository, times(1)).findById(testUserId);
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 사용자 없음")
    void updateUserPassword_Fail_UserNotFound() {
        // given
        ChangePasswordRequest request = new ChangePasswordRequest("newPassword123");

        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.updateUserPassword(testUserId, request))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, times(1)).findById(testUserId);
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("사용자 잠금 업데이트 실패 - 사용자 없음")
    void updateUserLock_Fail_UserNotFound() {
        // given
        UserLockUpdateRequest request = new UserLockUpdateRequest(true);

        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.updateUserLock(testUserId, request))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, times(1)).findById(testUserId);
    }

    @Test
    @DisplayName("사용자 프로필 업데이트 성공 - 기본 정보만")
    void updateUserProfile_Success_BasicInfo() {
        // given
        ProfileUpdateRequest request = new ProfileUpdateRequest(
                "updatedName",
                null,
                null,
                null,
                null
        );

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userMapper.toProfileDTO(any(User.class))).thenReturn(testProfileDTO);

        // when
        ProfileDTO result = userService.updateUserProfile(testUserId, request, null);

        // then
        assertThat(result).isNotNull();
        verify(userRepository, times(1)).findById(testUserId);
        verify(binaryContentService, never()).createBinaryContent(any());
    }

    @Test
    @DisplayName("사용자 프로필 업데이트 성공 - 프로필 이미지 포함")
    void updateUserProfile_Success_WithImage() {
        // given
        ProfileUpdateRequest request = new ProfileUpdateRequest(
                "updatedName",
                null,
                null,
                null,
                null
        );

        BinaryContent mockBinaryContent = BinaryContent.builder()
                .fileUrl("https://example.com/profile.jpg")
                .build();

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(binaryContentService.createBinaryContent(any())).thenReturn(mockBinaryContent);
        when(userMapper.toProfileDTO(any(User.class))).thenReturn(testProfileDTO);

        // when
        ProfileDTO result = userService.updateUserProfile(testUserId, request, mock(org.springframework.web.multipart.MultipartFile.class));

        // then
        assertThat(result).isNotNull();
        verify(userRepository, times(1)).findById(testUserId);
        verify(binaryContentService, times(1)).createBinaryContent(any());
    }

    @Test
    @DisplayName("사용자 프로필 업데이트 실패 - 사용자 없음")
    void updateUserProfile_Fail_UserNotFound() {
        // given
        ProfileUpdateRequest request = new ProfileUpdateRequest(
                "updatedName",
                null,
                null,
                null,
                null
        );

        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.updateUserProfile(testUserId, request, null))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, times(1)).findById(testUserId);
    }

    @Test
    @DisplayName("사용자 프로필 업데이트 성공 - 위치 정보 포함")
    void updateUserProfile_Success_WithLocation() {
        // given
        WeatherAPILocation location = new WeatherAPILocation(37.5, 127.0, null, null, null);
        ProfileUpdateRequest request = new ProfileUpdateRequest(
                "updatedName",
                null,
                null,
                location,
                null
        );

        WeatherRegion mockWeatherRegion = WeatherRegion.builder()
                .longitude(127.0)
                .latitude(37.5)
                .build();

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(weatherService.findWeatherRegion(127.0, 37.5)).thenReturn(mockWeatherRegion);
        when(userMapper.toProfileDTO(any(User.class))).thenReturn(testProfileDTO);

        // when
        ProfileDTO result = userService.updateUserProfile(testUserId, request, null);

        // then
        assertThat(result).isNotNull();
        verify(userRepository, times(1)).findById(testUserId);
        verify(weatherService, times(1)).findWeatherRegion(127.0, 37.5);
    }

    @Test
    @DisplayName("사용자 목록 조회 성공")
    void findUsers_Success() {
        // given
        UserDTOCursorResponse mockResponse = new UserDTOCursorResponse(
                List.of(testUserDTO),
                null,
                null,
                false,
                1L,
                "createdAt",
                "DESCENDING"
        );

        when(userRepository.findUsersByCursor(
                isNull(), isNull(), eq(20), eq("createdAt"), eq("DESCENDING"), isNull(), isNull(), isNull()
        )).thenReturn(mockResponse);

        // when
        UserDTOCursorResponse result = userService.findUsers(
                null, null, 20, "createdAt", "DESCENDING", null, null, null
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.data()).hasSize(1);
        assertThat(result.totalCount()).isEqualTo(1L);
        verify(userRepository, times(1)).findUsersByCursor(
                isNull(), isNull(), eq(20), eq("createdAt"), eq("DESCENDING"), isNull(), isNull(), isNull()
        );
    }
}
