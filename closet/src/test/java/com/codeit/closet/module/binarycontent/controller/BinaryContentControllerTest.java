package com.codeit.closet.module.binarycontent.controller;

import com.codeit.closet.module.binarycontent.entity.BinaryContent;
import com.codeit.closet.module.binarycontent.service.BinaryContentService;
import com.codeit.closet.module.binarycontent.storage.BinaryContentStorage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.NoSuchElementException;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@org.junit.jupiter.api.Disabled("Security 설정 문제로 임시 비활성화")
@WebMvcTest(value = BinaryContentController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
        })
@ActiveProfiles("test")
@DisplayName("BinaryContentController 테스트")
class BinaryContentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BinaryContentService binaryContentService;

    @MockBean
    private BinaryContentStorage binaryContentStorage;

    @Test
    @DisplayName("파일 업로드 성공")
    void saveBinaryContent_Success() throws Exception {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "multipartFile",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        UUID fileId = UUID.randomUUID();
        BinaryContent savedContent = BinaryContent.builder()
                .fileName("test.jpg")
                .contentType(MediaType.IMAGE_JPEG_VALUE)
                .size(file.getSize())
                .fileUrl("https://example.com/" + fileId)
                .build();

        when(binaryContentService.createBinaryContent(any())).thenReturn(savedContent);

        // when & then
        mockMvc.perform(multipart("/api/binaryContents")
                        .file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fileName").value("test.jpg"))
                .andExpect(jsonPath("$.contentType").value(MediaType.IMAGE_JPEG_VALUE));

        verify(binaryContentService, times(1)).createBinaryContent(any());
    }

    @Test
    @DisplayName("파일 URL 조회 성공")
    void getFileUrl_Success() throws Exception {
        // given
        UUID fileId = UUID.randomUUID();
        String fileUrl = "https://example.com/" + fileId;

        when(binaryContentService.findFileUrlByBinaryContentId(fileId)).thenReturn(fileUrl);

        // when & then
        mockMvc.perform(get("/api/binaryContents/findUrl/{id}", fileId))
                .andExpect(status().isOk())
                .andExpect(content().string(fileUrl));

        verify(binaryContentService, times(1)).findFileUrlByBinaryContentId(fileId);
    }

    @Test
    @DisplayName("존재하지 않는 파일 URL 조회 시 예외 발생")
    void getFileUrl_NotFound() throws Exception {
        // given
        UUID nonExistentId = UUID.randomUUID();

        when(binaryContentService.findFileUrlByBinaryContentId(nonExistentId))
                .thenThrow(new IllegalArgumentException("BinaryContent를 찾을 수 없습니다."));

        // when & then
        mockMvc.perform(get("/api/binaryContents/findUrl/{id}", nonExistentId))
                .andExpect(status().is4xxClientError());

        verify(binaryContentService, times(1)).findFileUrlByBinaryContentId(nonExistentId);
    }

    @Test
    @DisplayName("파일 다운로드 성공")
    void downloadBinaryContent_Success() throws Exception {
        // given
        UUID fileId = UUID.randomUUID();
        BinaryContent binaryContent = BinaryContent.builder()
                .fileName("download-test.pdf")
                .contentType("application/pdf")
                .size(1024L)
                .fileUrl("https://example.com/" + fileId)
                .build();

        byte[] fileContent = "PDF file content".getBytes();
        Resource resource = new ByteArrayResource(fileContent);

        ResponseEntity<Resource> responseEntity = ResponseEntity
                .status(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + binaryContent.getFileName() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, binaryContent.getContentType())
                .body(resource);

        when(binaryContentService.findByBinaryContentId(fileId)).thenReturn(binaryContent);
        when(binaryContentStorage.download(binaryContent)).thenReturn(responseEntity);

        // when & then
        mockMvc.perform(get("/api/binaryContents/{binaryContentId}/download", fileId))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"download-test.pdf\""))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/pdf"));

        verify(binaryContentService, times(1)).findByBinaryContentId(fileId);
        verify(binaryContentStorage, times(1)).download(binaryContent);
    }

    @Test
    @DisplayName("존재하지 않는 파일 다운로드 시 예외 발생")
    void downloadBinaryContent_NotFound() throws Exception {
        // given
        UUID nonExistentId = UUID.randomUUID();

        when(binaryContentService.findByBinaryContentId(nonExistentId))
                .thenThrow(new NoSuchElementException("존재하지않는 binaryContent 입니다."));

        // when & then
        mockMvc.perform(get("/api/binaryContents/{binaryContentId}/download", nonExistentId))
                .andExpect(status().is4xxClientError());

        verify(binaryContentService, times(1)).findByBinaryContentId(nonExistentId);
        verify(binaryContentStorage, never()).download(any());
    }

    @Test
    @DisplayName("파일 삭제 성공")
    void deleteBinaryContent_Success() throws Exception {
        // given
        UUID fileId = UUID.randomUUID();

        doNothing().when(binaryContentService).deleteBinaryContent(fileId);

        // when & then
        mockMvc.perform(delete("/api/binaryContents/{binaryContentId}", fileId))
                .andExpect(status().isOk());

        verify(binaryContentService, times(1)).deleteBinaryContent(fileId);
    }

    @Test
    @DisplayName("존재하지 않는 파일 삭제 시 예외 발생")
    void deleteBinaryContent_NotFound() throws Exception {
        // given
        UUID nonExistentId = UUID.randomUUID();

        doThrow(new IllegalArgumentException("BinaryContent를 찾을 수 없습니다."))
                .when(binaryContentService).deleteBinaryContent(nonExistentId);

        // when & then
        mockMvc.perform(delete("/api/binaryContents/{binaryContentId}", nonExistentId))
                .andExpect(status().is4xxClientError());

        verify(binaryContentService, times(1)).deleteBinaryContent(nonExistentId);
    }

    @Test
    @DisplayName("대용량 파일 업로드")
    void saveLargeFile_Success() throws Exception {
        // given
        byte[] largeContent = new byte[10 * 1024 * 1024]; // 10MB
        MockMultipartFile largeFile = new MockMultipartFile(
                "multipartFile",
                "large-video.mp4",
                "video/mp4",
                largeContent
        );

        UUID fileId = UUID.randomUUID();
        BinaryContent savedContent = BinaryContent.builder()
                .fileName("large-video.mp4")
                .contentType("video/mp4")
                .size((long) largeContent.length)
                .fileUrl("https://example.com/" + fileId)
                .build();

        when(binaryContentService.createBinaryContent(any())).thenReturn(savedContent);

        // when & then
        mockMvc.perform(multipart("/api/binaryContents")
                        .file(largeFile))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fileName").value("large-video.mp4"))
                .andExpect(jsonPath("$.contentType").value("video/mp4"));

        verify(binaryContentService, times(1)).createBinaryContent(any());
    }

    @Test
    @DisplayName("다양한 파일 타입 업로드")
    void saveVariousFileTypes_Success() throws Exception {
        // given - PDF file
        MockMultipartFile pdfFile = new MockMultipartFile(
                "multipartFile",
                "document.pdf",
                "application/pdf",
                "PDF content".getBytes()
        );

        UUID pdfId = UUID.randomUUID();
        BinaryContent savedPdf = BinaryContent.builder()
                .fileName("document.pdf")
                .contentType("application/pdf")
                .size(pdfFile.getSize())
                .fileUrl("https://example.com/" + pdfId)
                .build();

        when(binaryContentService.createBinaryContent(any())).thenReturn(savedPdf);

        // when & then
        mockMvc.perform(multipart("/api/binaryContents")
                        .file(pdfFile))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fileName").value("document.pdf"))
                .andExpect(jsonPath("$.contentType").value("application/pdf"));

        verify(binaryContentService, times(1)).createBinaryContent(any());
    }
}
