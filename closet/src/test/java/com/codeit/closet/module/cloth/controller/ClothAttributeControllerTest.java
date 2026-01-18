package com.codeit.closet.module.cloth.controller;

import com.codeit.closet.module.cloth.dto.ClothAttributeCreateRequest;
import com.codeit.closet.module.cloth.dto.ClothAttributeDTO;
import com.codeit.closet.module.cloth.dto.ClothAttributeUpdateRequest;
import com.codeit.closet.module.cloth.exception.ClothAttributeNotFoundException;
import com.codeit.closet.module.cloth.service.ClothAttributeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = ClothAttributeController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
        },
        excludeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
                type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                classes = com.codeit.closet.common.config.SecurityConfig.class
        ))
@org.springframework.context.annotation.Import({com.codeit.closet.common.config.TestSecurityConfig.class, com.codeit.closet.common.exception.GlobalExceptionHandler.class})
@ActiveProfiles("test")
@DisplayName("ClothAttributeController 테스트")
class ClothAttributeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClothAttributeService clothAttributeService;

    private UUID testAttributeId;
    private ClothAttributeDTO testAttributeDTO;

    @BeforeEach
    void setUp() {
        testAttributeId = UUID.randomUUID();
        testAttributeDTO = new ClothAttributeDTO(
                testAttributeId,
                "색상",
                List.of("블랙", "화이트", "블루", "레드"),
                java.time.Instant.now()
        );
    }

    @Test
    @DisplayName("속성 정의 생성 성공")
    void createClothesAttribute_Success() throws Exception {
        // given
        ClothAttributeCreateRequest request = new ClothAttributeCreateRequest(
                "색상",
                List.of("블랙", "화이트", "블루", "레드")
        );

        when(clothAttributeService.createClothAttribute(any(ClothAttributeCreateRequest.class)))
                .thenReturn(testAttributeDTO);

        // when & then
        mockMvc.perform(post("/api/clothes/attribute-defs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(testAttributeId.toString()))
                .andExpect(jsonPath("$.name").value("색상"))
                .andExpect(jsonPath("$.selectableValues").isArray())
                .andExpect(jsonPath("$.selectableValues[0]").value("블랙"));

        verify(clothAttributeService, times(1)).createClothAttribute(any(ClothAttributeCreateRequest.class));
    }

    @Test
    @DisplayName("속성 정의 목록 조회 성공")
    void getClothesAttributes_Success() throws Exception {
        // given
        ClothAttributeDTO attribute2 = new ClothAttributeDTO(
                UUID.randomUUID(),
                "사이즈",
                List.of("S", "M", "L", "XL"),
                java.time.Instant.now()
        );

        List<ClothAttributeDTO> attributeList = List.of(testAttributeDTO, attribute2);

        when(clothAttributeService.findAllClothAttributes()).thenReturn(attributeList);

        // when & then
        mockMvc.perform(get("/api/clothes/attribute-defs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("색상"))
                .andExpect(jsonPath("$[1].name").value("사이즈"));

        verify(clothAttributeService, times(1)).findAllClothAttributes();
    }

    @Test
    @DisplayName("속성 정의 단건 조회 성공")
    void findClothesAttribute_Success() throws Exception {
        // given
        when(clothAttributeService.findClothAttribute(testAttributeId)).thenReturn(testAttributeDTO);

        // when & then
        mockMvc.perform(get("/api/clothes/attribute-defs/{attributeId}", testAttributeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testAttributeId.toString()))
                .andExpect(jsonPath("$.name").value("색상"));

        verify(clothAttributeService, times(1)).findClothAttribute(testAttributeId);
    }

    @Test
    @DisplayName("속성 정의 단건 조회 실패 - 존재하지 않는 ID")
    void findClothesAttribute_NotFound() throws Exception {
        // given
        UUID nonExistentId = UUID.randomUUID();
        when(clothAttributeService.findClothAttribute(nonExistentId))
                .thenThrow(new ClothAttributeNotFoundException(nonExistentId));

        // when & then
        mockMvc.perform(get("/api/clothes/attribute-defs/{attributeId}", nonExistentId))
                .andExpect(status().is4xxClientError());

        verify(clothAttributeService, times(1)).findClothAttribute(nonExistentId);
    }

    @Test
    @DisplayName("속성 정의 수정 성공")
    void updateClothesAttribute_Success() throws Exception {
        // given
        ClothAttributeUpdateRequest request = new ClothAttributeUpdateRequest(
                "컬러",
                List.of("블랙", "화이트", "블루", "레드", "그레이")
        );

        ClothAttributeDTO updatedDTO = new ClothAttributeDTO(
                testAttributeId,
                "컬러",
                List.of("블랙", "화이트", "블루", "레드", "그레이"),
                java.time.Instant.now()
        );

        when(clothAttributeService.updateClothAttribute(any(UUID.class), any(ClothAttributeUpdateRequest.class)))
                .thenReturn(updatedDTO);

        // when & then
        mockMvc.perform(patch("/api/clothes/attribute-defs/{attributeId}", testAttributeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("컬러"))
                .andExpect(jsonPath("$.selectableValues.length()").value(5));

        verify(clothAttributeService, times(1)).updateClothAttribute(any(UUID.class), any(ClothAttributeUpdateRequest.class));
    }

    @Test
    @DisplayName("속성 정의 삭제 성공")
    void deleteClothesAttribute_Success() throws Exception {
        // given
        doNothing().when(clothAttributeService).deleteClothAttribute(testAttributeId);

        // when & then
        mockMvc.perform(delete("/api/clothes/attribute-defs/{attributeId}", testAttributeId))
                .andExpect(status().isNoContent());

        verify(clothAttributeService, times(1)).deleteClothAttribute(testAttributeId);
    }

    @Test
    @DisplayName("속성 정의 삭제 실패 - 존재하지 않는 ID")
    void deleteClothesAttribute_NotFound() throws Exception {
        // given
        UUID nonExistentId = UUID.randomUUID();
        doThrow(new ClothAttributeNotFoundException(nonExistentId))
                .when(clothAttributeService).deleteClothAttribute(nonExistentId);

        // when & then
        mockMvc.perform(delete("/api/clothes/attribute-defs/{attributeId}", nonExistentId))
                .andExpect(status().is4xxClientError());

        verify(clothAttributeService, times(1)).deleteClothAttribute(nonExistentId);
    }

    @Test
    @DisplayName("빈 값 목록으로 속성 정의 생성")
    void createClothesAttribute_WithEmptyValues() throws Exception {
        // given
        ClothAttributeCreateRequest request = new ClothAttributeCreateRequest(
                "빈속성",
                List.of()
        );

        ClothAttributeDTO responseDTO = new ClothAttributeDTO(
                UUID.randomUUID(),
                "빈속성",
                List.of(),
                java.time.Instant.now()
        );

        when(clothAttributeService.createClothAttribute(any(ClothAttributeCreateRequest.class)))
                .thenReturn(responseDTO);

        // when & then
        mockMvc.perform(post("/api/clothes/attribute-defs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.selectableValues").isEmpty());

        verify(clothAttributeService, times(1)).createClothAttribute(any(ClothAttributeCreateRequest.class));
    }

    @Test
    @DisplayName("속성 정의 부분 수정 - 이름만 변경")
    void updateClothesAttribute_NameOnly() throws Exception {
        // given
        ClothAttributeUpdateRequest request = new ClothAttributeUpdateRequest(
                "색깔",
                null
        );

        ClothAttributeDTO updatedDTO = new ClothAttributeDTO(
                testAttributeId,
                "색깔",
                List.of("블랙", "화이트", "블루", "레드"),
                java.time.Instant.now()
        );

        when(clothAttributeService.updateClothAttribute(any(UUID.class), any(ClothAttributeUpdateRequest.class)))
                .thenReturn(updatedDTO);

        // when & then
        mockMvc.perform(patch("/api/clothes/attribute-defs/{attributeId}", testAttributeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("색깔"));

        verify(clothAttributeService, times(1)).updateClothAttribute(any(UUID.class), any(ClothAttributeUpdateRequest.class));
    }

    @Test
    @DisplayName("속성 정의 부분 수정 - 값 목록만 변경")
    void updateClothesAttribute_ValuesOnly() throws Exception {
        // given
        ClothAttributeUpdateRequest request = new ClothAttributeUpdateRequest(
                null,
                List.of("블랙", "화이트", "블루")
        );

        ClothAttributeDTO updatedDTO = new ClothAttributeDTO(
                testAttributeId,
                "색상",
                List.of("블랙", "화이트", "블루"),
                java.time.Instant.now()
        );

        when(clothAttributeService.updateClothAttribute(any(UUID.class), any(ClothAttributeUpdateRequest.class)))
                .thenReturn(updatedDTO);

        // when & then
        mockMvc.perform(patch("/api/clothes/attribute-defs/{attributeId}", testAttributeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.selectableValues.length()").value(3));

        verify(clothAttributeService, times(1)).updateClothAttribute(any(UUID.class), any(ClothAttributeUpdateRequest.class));
    }
}
