package com.codeit.closet.module.user.controller;

import com.codeit.closet.common.exception.user.DuplicateUserException;
import com.codeit.closet.common.exception.user.UserNotFoundException;
import com.codeit.closet.common.security.ClosetUserDetails;
import com.codeit.closet.module.user.dto.profile.ProfileDTO;
import com.codeit.closet.module.user.dto.profile.ProfileUpdateRequest;
import com.codeit.closet.module.user.dto.user.ChangePasswordRequest;
import com.codeit.closet.module.user.dto.user.UserCreateRequest;
import com.codeit.closet.module.user.dto.user.UserDTO;
import com.codeit.closet.module.user.dto.user.UserDTOCursorResponse;
import com.codeit.closet.module.user.dto.user.UserLockUpdateRequest;
import com.codeit.closet.module.user.dto.user.UserRoleUpdateRequest;
import com.codeit.closet.module.user.entity.UserRole;
import com.codeit.closet.module.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = UserController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
        },
        excludeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
                type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                classes = com.codeit.closet.common.config.SecurityConfig.class
        ))
@Import({com.codeit.closet.common.config.TestSecurityConfig.class, com.codeit.closet.common.exception.GlobalExceptionHandler.class})
@ActiveProfiles("test")
@DisplayName("UserController 테스트")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UUID testUserId;
    private UserDTO testUserDTO;
    private ProfileDTO testProfileDTO;
    private ClosetUserDetails mockUserDetails;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();

        testUserDTO = new UserDTO(
                testUserId,
                Instant.now(),
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

        mockUserDetails = new ClosetUserDetails(testUserDTO, "password123", null, null);
    }

    @Test
    @DisplayName("사용자 생성 성공")
    void createUser_Success() throws Exception {
        // given
        UserCreateRequest request = new UserCreateRequest("testuser", "password123", "test@example.com");

        when(userService.createUser(any(UserCreateRequest.class))).thenReturn(testUserDTO);

        // when & then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("testuser"));

        verify(userService, times(1)).createUser(any(UserCreateRequest.class));
    }

    @Test
    @DisplayName("사용자 생성 실패 - 중복 이메일")
    void createUser_Fail_DuplicateEmail() throws Exception {
        // given
        UserCreateRequest request = new UserCreateRequest("testuser", "password123", "test@example.com");

        when(userService.createUser(any(UserCreateRequest.class)))
                .thenThrow(DuplicateUserException.withEmail("test@example.com"));

        // when & then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());

        verify(userService, times(1)).createUser(any(UserCreateRequest.class));
    }

    @Test
    @DisplayName("사용자 프로필 조회 성공")
    void getUserProfile_Success() throws Exception {
        // given
        when(userService.findUserProfile(testUserId)).thenReturn(testProfileDTO);

        // when & then
        mockMvc.perform(get("/api/users/{userId}/profiles", testUserId)
                        .with(user(mockUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(testUserId.toString()))
                .andExpect(jsonPath("$.name").value("testuser"));

        verify(userService, times(1)).findUserProfile(testUserId);
    }

    @Test
    @DisplayName("사용자 프로필 조회 실패 - 존재하지 않는 사용자")
    void getUserProfile_UserNotFound() throws Exception {
        // given
        when(userService.findUserProfile(testUserId))
                .thenThrow(new java.util.NoSuchElementException("존재하지 않는 회원입니다."));

        // when & then
        mockMvc.perform(get("/api/users/{userId}/profiles", testUserId)
                        .with(user(mockUserDetails)))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).findUserProfile(testUserId);
    }

    @Test
    @DisplayName("사용자 역할 업데이트 성공")
    void updateUserRole_Success() throws Exception {
        // given
        UserRoleUpdateRequest request = new UserRoleUpdateRequest(UserRole.ADMIN);
        UserDTO updatedUserDTO = new UserDTO(
                testUserId,
                Instant.now(),
                "test@example.com",
                "testuser",
                UserRole.ADMIN,
                false
        );

        when(userService.updateUserRole(eq(testUserId), any(UserRoleUpdateRequest.class)))
                .thenReturn(updatedUserDTO);

        // when & then
        mockMvc.perform(patch("/api/users/{userId}/role", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(mockUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));

        verify(userService, times(1)).updateUserRole(eq(testUserId), any(UserRoleUpdateRequest.class));
    }

    @Test
    @DisplayName("비밀번호 변경 성공")
    void updateUserPassword_Success() throws Exception {
        // given
        ChangePasswordRequest request = new ChangePasswordRequest("newPassword123");

        doNothing().when(userService).updateUserPassword(eq(testUserId), any(ChangePasswordRequest.class));

        // when & then
        mockMvc.perform(patch("/api/users/{userId}/password", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(mockUserDetails)))
                .andExpect(status().isOk());

        verify(userService, times(1)).updateUserPassword(eq(testUserId), any(ChangePasswordRequest.class));
    }

    @Test
    @DisplayName("사용자 잠금 업데이트 성공")
    void updateUserLock_Success() throws Exception {
        // given
        UserLockUpdateRequest request = new UserLockUpdateRequest(true);
        UserDTO lockedUserDTO = new UserDTO(
                testUserId,
                Instant.now(),
                "test@example.com",
                "testuser",
                UserRole.USER,
                true
        );

        when(userService.updateUserLock(eq(testUserId), any(UserLockUpdateRequest.class)))
                .thenReturn(lockedUserDTO);

        // when & then
        mockMvc.perform(patch("/api/users/{userId}/lock", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(mockUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.locked").value(true));

        verify(userService, times(1)).updateUserLock(eq(testUserId), any(UserLockUpdateRequest.class));
    }

    @Test
    @DisplayName("사용자 업데이트 실패 - 존재하지 않는 사용자")
    void updateUserRole_UserNotFound() throws Exception {
        // given
        UserRoleUpdateRequest request = new UserRoleUpdateRequest(UserRole.ADMIN);

        when(userService.updateUserRole(eq(testUserId), any(UserRoleUpdateRequest.class)))
                .thenThrow(new UserNotFoundException());

        // when & then
        mockMvc.perform(patch("/api/users/{userId}/role", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(mockUserDetails)))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).updateUserRole(eq(testUserId), any(UserRoleUpdateRequest.class));
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 존재하지 않는 사용자")
    void updateUserPassword_UserNotFound() throws Exception {
        // given
        ChangePasswordRequest request = new ChangePasswordRequest("newPassword123");

        doThrow(new UserNotFoundException()).when(userService)
                .updateUserPassword(eq(testUserId), any(ChangePasswordRequest.class));

        // when & then
        mockMvc.perform(patch("/api/users/{userId}/password", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(mockUserDetails)))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).updateUserPassword(eq(testUserId), any(ChangePasswordRequest.class));
    }

    @Test
    @DisplayName("사용자 잠금 업데이트 실패 - 존재하지 않는 사용자")
    void updateUserLock_UserNotFound() throws Exception {
        // given
        UserLockUpdateRequest request = new UserLockUpdateRequest(true);

        when(userService.updateUserLock(eq(testUserId), any(UserLockUpdateRequest.class)))
                .thenThrow(new UserNotFoundException());

        // when & then
        mockMvc.perform(patch("/api/users/{userId}/lock", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(mockUserDetails)))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).updateUserLock(eq(testUserId), any(UserLockUpdateRequest.class));
    }

    @Test
    @DisplayName("사용자 목록 조회 성공")
    void getUsers_Success() throws Exception {
        // given
        UserDTOCursorResponse response = new UserDTOCursorResponse(
                List.of(testUserDTO),
                null,
                null,
                false,
                1L,
                "createdAt",
                "DESCENDING"
        );

        when(userService.findUsers(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/users")
                        .param("limit", "20")
                        .param("sortBy", "createdAt")
                        .param("sortDirection", "DESCENDING")
                        .with(user(mockUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.totalCount").value(1));

        verify(userService, times(1)).findUsers(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("사용자 프로필 업데이트 성공 - 이미지 없이")
    void updateUserProfile_Success_WithoutImage() throws Exception {
        // given
        ProfileUpdateRequest request = new ProfileUpdateRequest(
                "updatedName",
                null,
                null,
                null,
                null
        );

        when(userService.updateUserProfile(eq(testUserId), any(ProfileUpdateRequest.class), any()))
                .thenReturn(testProfileDTO);

        // when & then
        mockMvc.perform(multipart("/api/users/{userId}/profiles", testUserId)
                        .file(new MockMultipartFile("request", "", "application/json",
                                objectMapper.writeValueAsBytes(request)))
                        .with(requestProcessor -> {
                            requestProcessor.setMethod("PATCH");
                            return requestProcessor;
                        })
                        .with(user(mockUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(testUserId.toString()));

        verify(userService, times(1)).updateUserProfile(eq(testUserId), any(ProfileUpdateRequest.class), any());
    }

    @Test
    @DisplayName("사용자 프로필 업데이트 성공 - 이미지 포함")
    void updateUserProfile_Success_WithImage() throws Exception {
        // given
        ProfileUpdateRequest request = new ProfileUpdateRequest(
                "updatedName",
                null,
                null,
                null,
                null
        );

        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "profile.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        when(userService.updateUserProfile(eq(testUserId), any(ProfileUpdateRequest.class), any()))
                .thenReturn(testProfileDTO);

        // when & then
        mockMvc.perform(multipart("/api/users/{userId}/profiles", testUserId)
                        .file(new MockMultipartFile("request", "", "application/json",
                                objectMapper.writeValueAsBytes(request)))
                        .file(imageFile)
                        .with(requestProcessor -> {
                            requestProcessor.setMethod("PATCH");
                            return requestProcessor;
                        })
                        .with(user(mockUserDetails)))
                .andExpect(status().isOk());

        verify(userService, times(1)).updateUserProfile(eq(testUserId), any(ProfileUpdateRequest.class), any());
    }

    @Test
    @DisplayName("사용자 프로필 업데이트 실패 - 존재하지 않는 사용자")
    void updateUserProfile_UserNotFound() throws Exception {
        // given
        ProfileUpdateRequest request = new ProfileUpdateRequest(
                "updatedName",
                null,
                null,
                null,
                null
        );

        when(userService.updateUserProfile(eq(testUserId), any(ProfileUpdateRequest.class), any()))
                .thenThrow(new UserNotFoundException());

        // when & then
        mockMvc.perform(multipart("/api/users/{userId}/profiles", testUserId)
                        .file(new MockMultipartFile("request", "", "application/json",
                                objectMapper.writeValueAsBytes(request)))
                        .with(requestProcessor -> {
                            requestProcessor.setMethod("PATCH");
                            return requestProcessor;
                        })
                        .with(user(mockUserDetails)))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).updateUserProfile(eq(testUserId), any(ProfileUpdateRequest.class), any());
    }
}
