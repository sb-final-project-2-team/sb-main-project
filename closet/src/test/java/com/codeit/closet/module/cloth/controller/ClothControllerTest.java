package com.codeit.closet.module.cloth.controller;

import com.codeit.closet.common.security.ClosetUserDetails;
import com.codeit.closet.module.cloth.dto.ClothCreateRequest;
import com.codeit.closet.module.cloth.dto.ClothDTO;
import com.codeit.closet.module.cloth.dto.ClothDTOCursorResponse;
import com.codeit.closet.module.cloth.dto.ClothUpdateRequest;
import com.codeit.closet.module.cloth.exception.ClothNotFoundException;
import com.codeit.closet.module.cloth.service.ClothExtractionService;
import com.codeit.closet.module.cloth.service.ClothService;
import com.codeit.closet.module.user.dto.user.UserDTO;
import com.codeit.closet.module.user.entity.UserRole;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = ClothController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
        },
        excludeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
                type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                classes = com.codeit.closet.common.config.SecurityConfig.class
        ))
@Import(com.codeit.closet.common.config.TestSecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("ClothController 테스트")
class ClothControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClothService clothService;

    @MockBean
    private ClothExtractionService clothExtractionService;

    private UUID testUserId;
    private UUID testClothId;
    private ClothDTO testClothDTO;
    private ClosetUserDetails mockUserDetails;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testClothId = UUID.randomUUID();

        testClothDTO = new ClothDTO(
                testClothId,
                testUserId,
                "블루 데님 자켓",
                "https://example.com/image.jpg",
                "OUTER",
                new ArrayList<>()
        );

        UserDTO userDTO = new UserDTO(
                testUserId,
                java.time.Instant.now(),
                "test@example.com",
                "testuser",
                UserRole.USER,
                false
        );
        mockUserDetails = new ClosetUserDetails(userDTO, "password123", null, null);
    }

    @Test
    @DisplayName("의상 등록 성공")
    void createClothes_Success() throws Exception {
        // given
        ClothCreateRequest request = new ClothCreateRequest(
                testUserId,
                "블루 데님 자켓",
                "OUTER",
                new ArrayList<>()
        );

        MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
        );

        MockMultipartFile imagePart = new MockMultipartFile(
                "image",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        when(clothService.createCloth(any(ClothCreateRequest.class), any())).thenReturn(testClothDTO);

        // when & then
        mockMvc.perform(multipart("/api/clothes")
                        .file(requestPart)
                        .file(imagePart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(testClothId.toString()))
                .andExpect(jsonPath("$.name").value("블루 데님 자켓"))
                .andExpect(jsonPath("$.type").value("OUTER"));

        verify(clothService, times(1)).createCloth(any(ClothCreateRequest.class), any());
    }

    @Test
    @DisplayName("의상 등록 - 이미지 없이")
    void createClothes_WithoutImage() throws Exception {
        // given
        ClothCreateRequest request = new ClothCreateRequest(
                testUserId,
                "화이트 티셔츠",
                "TOP",
                new ArrayList<>()
        );

        MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
        );

        ClothDTO responseDTO = new ClothDTO(
                testClothId,
                testUserId,
                "화이트 티셔츠",
                null,
                "TOP",
                new ArrayList<>()
        );

        when(clothService.createCloth(any(ClothCreateRequest.class), isNull())).thenReturn(responseDTO);

        // when & then
        mockMvc.perform(multipart("/api/clothes")
                        .file(requestPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("화이트 티셔츠"))
                .andExpect(jsonPath("$.imageUrl").doesNotExist());

        verify(clothService, times(1)).createCloth(any(ClothCreateRequest.class), isNull());
    }

    @Test
    @DisplayName("의상 목록 조회 성공")
    void getClothes_Success() throws Exception {
        // given
        List<ClothDTO> clothList = List.of(testClothDTO);
        ClothDTOCursorResponse response = new ClothDTOCursorResponse(
                clothList,
                null,
                null,
                false,
                1L,
                "createdAt",
                "DESCENDING"
        );

        when(clothService.findAllCloths(any(), any(), any(), anyInt(), any(), any(), any()))
                .thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/clothes")
                        .param("ownerId", testUserId.toString())
                        .param("limit", "20")
                        .with(user(mockUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("블루 데님 자켓"))
                .andExpect(jsonPath("$.totalCount").value(1));

        verify(clothService, times(1)).findAllCloths(any(), any(), any(), anyInt(), any(), any(), any());
    }

    @Test
    @DisplayName("의상 단건 조회 성공")
    void findClothes_Success() throws Exception {
        // given
        when(clothService.findCloth(testClothId)).thenReturn(testClothDTO);

        // when & then
        mockMvc.perform(get("/api/clothes/{clothId}", testClothId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testClothId.toString()))
                .andExpect(jsonPath("$.name").value("블루 데님 자켓"));

        verify(clothService, times(1)).findCloth(testClothId);
    }

    @Test
    @DisplayName("의상 단건 조회 실패 - 존재하지 않는 ID")
    void findClothes_NotFound() throws Exception {
        // given
        UUID nonExistentId = UUID.randomUUID();
        when(clothService.findCloth(nonExistentId))
                .thenThrow(new ClothNotFoundException(nonExistentId));

        // when & then
        mockMvc.perform(get("/api/clothes/{clothId}", nonExistentId))
                .andExpect(status().is4xxClientError());

        verify(clothService, times(1)).findCloth(nonExistentId);
    }

    @Test
    @DisplayName("의상 수정 성공")
    void updateClothes_Success() throws Exception {
        // given
        ClothUpdateRequest request = new ClothUpdateRequest(
                "다크 블루 데님 자켓",
                "OUTER",
                new ArrayList<>()
        );

        MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
        );

        ClothDTO updatedDTO = new ClothDTO(
                testClothId,
                testUserId,
                "다크 블루 데님 자켓",
                "https://example.com/image.jpg",
                "OUTER",
                new ArrayList<>()
        );

        when(clothService.updateCloth(any(UUID.class), any(ClothUpdateRequest.class), any(UUID.class), anyBoolean(), any()))
                .thenReturn(updatedDTO);

        // when & then
        mockMvc.perform(multipart("/api/clothes/{clothId}", testClothId)
                        .file(requestPart)
                        .with(request1 -> {
                            request1.setMethod("PATCH");
                            return request1;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(user(mockUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("다크 블루 데님 자켓"));

        verify(clothService, times(1)).updateCloth(any(UUID.class), any(ClothUpdateRequest.class), any(UUID.class), anyBoolean(), any());
    }

    @Test
    @DisplayName("의상 삭제 성공")
    void deleteClothes_Success() throws Exception {
        // given
        doNothing().when(clothService).deleteCloth(any(UUID.class), any(UUID.class), anyBoolean());

        // when & then
        mockMvc.perform(delete("/api/clothes/{clothId}", testClothId)
                        .with(user(mockUserDetails)))
                .andExpect(status().isNoContent());

        verify(clothService, times(1)).deleteCloth(any(UUID.class), any(UUID.class), anyBoolean());
    }

    @Test
    @DisplayName("의상 삭제 실패 - 존재하지 않는 ID")
    void deleteClothes_NotFound() throws Exception {
        // given
        UUID nonExistentId = UUID.randomUUID();
        doThrow(new ClothNotFoundException(nonExistentId))
                .when(clothService).deleteCloth(any(UUID.class), any(UUID.class), anyBoolean());

        // when & then
        mockMvc.perform(delete("/api/clothes/{clothId}", nonExistentId)
                        .with(user(mockUserDetails)))
                .andExpect(status().is4xxClientError());

        verify(clothService, times(1)).deleteCloth(any(UUID.class), any(UUID.class), anyBoolean());
    }

    @Test
    @DisplayName("구매 링크로 의상 정보 추출 성공")
    void getClothesExtractions_Success() throws Exception {
        // given
        String url = "https://www.musinsa.com/app/goods/12345";
        when(clothExtractionService.extract(url)).thenReturn(testClothDTO);

        // when & then
        mockMvc.perform(get("/api/clothes/extractions")
                        .param("url", url))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("블루 데님 자켓"));

        verify(clothExtractionService, times(1)).extract(url);
    }

    @Test
    @DisplayName("정렬 옵션과 함께 의상 목록 조회")
    void getClothes_WithSorting() throws Exception {
        // given
        List<ClothDTO> clothList = List.of(testClothDTO);
        ClothDTOCursorResponse response = new ClothDTOCursorResponse(
                clothList,
                null,
                null,
                false,
                1L,
                "name",
                "ASCENDING"
        );

        when(clothService.findAllCloths(any(), any(), any(), anyInt(), eq("name"), eq("ASCENDING"), any()))
                .thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/clothes")
                        .param("ownerId", testUserId.toString())
                        .param("limit", "20")
                        .param("sortBy", "name")
                        .param("sortDirection", "ASCENDING")
                        .with(user(mockUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sortBy").value("name"))
                .andExpect(jsonPath("$.sortDirection").value("ASCENDING"));

        verify(clothService, times(1)).findAllCloths(any(), any(), any(), anyInt(), eq("name"), eq("ASCENDING"), any());
    }

    @Test
    @DisplayName("타입 필터와 함께 의상 목록 조회")
    void getClothes_WithTypeFilter() throws Exception {
        // given
        List<ClothDTO> clothList = List.of(testClothDTO);
        ClothDTOCursorResponse response = new ClothDTOCursorResponse(
                clothList,
                null,
                null,
                false,
                1L,
                "createdAt",
                "DESCENDING"
        );

        when(clothService.findAllCloths(any(), any(), any(), anyInt(), any(), any(), eq("OUTER")))
                .thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/clothes")
                        .param("ownerId", testUserId.toString())
                        .param("limit", "20")
                        .param("typeEqual", "OUTER")
                        .with(user(mockUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].type").value("OUTER"));

        verify(clothService, times(1)).findAllCloths(any(), any(), any(), anyInt(), any(), any(), eq("OUTER"));
    }

    @Test
    @DisplayName("커서 페이징과 함께 의상 목록 조회")
    void getClothes_WithCursor() throws Exception {
        // given
        String cursor = "eyJpZCI6IjNmYTg1ZjY0In0=";
        UUID idAfter = UUID.randomUUID();

        List<ClothDTO> clothList = List.of(testClothDTO);
        ClothDTOCursorResponse response = new ClothDTOCursorResponse(
                clothList,
                cursor,
                idAfter,
                true,
                10L,
                "createdAt",
                "DESCENDING"
        );

        when(clothService.findAllCloths(any(), eq(cursor), eq(idAfter), anyInt(), any(), any(), any()))
                .thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/clothes")
                        .param("ownerId", testUserId.toString())
                        .param("cursor", cursor)
                        .param("idAfter", idAfter.toString())
                        .param("limit", "20")
                        .with(user(mockUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.nextCursor").value(cursor));

        verify(clothService, times(1)).findAllCloths(any(), eq(cursor), eq(idAfter), anyInt(), any(), any(), any());
    }
}
