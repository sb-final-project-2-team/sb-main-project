package com.codeit.closet.module.binarycontent.controller;

import com.codeit.closet.module.binarycontent.entity.BinaryContent;
import com.codeit.closet.module.binarycontent.repository.BinaryContentRepository;
import com.codeit.closet.module.binarycontent.service.BinaryContentService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"spring.security.enabled=false"})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
@DisplayName("BinaryContentController 통합 테스트")
class BinaryContentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BinaryContentService binaryContentService;

    @Autowired
    private BinaryContentRepository binaryContentRepository;

    private BinaryContent testBinaryContent;

    @BeforeEach
    void setUp() {
        binaryContentRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        binaryContentRepository.deleteAll();
    }

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

        // when & then
        mockMvc.perform(multipart("/api/binaryContents")
                        .file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fileName").value("test.jpg"))
                .andExpect(jsonPath("$.contentType").value(MediaType.IMAGE_JPEG_VALUE))
                .andExpect(jsonPath("$.size").value(file.getSize()))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.fileUrl").exists());
    }

    @Test
    @DisplayName("파일 URL 조회 성공")
    void getFileUrl_Success() throws Exception {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "test pdf content".getBytes()
        );
        BinaryContent saved = binaryContentService.createBinaryContent(file);

        // when & then
        mockMvc.perform(get("/api/binaryContents/findUrl/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(saved.getId().toString())));
    }

    @Test
    @DisplayName("존재하지 않는 파일 URL 조회 시 예외 발생")
    void getFileUrl_NotFound() throws Exception {
        // given
        UUID nonExistentId = UUID.randomUUID();

        // when & then
        mockMvc.perform(get("/api/binaryContents/findUrl/{id}", nonExistentId))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("파일 다운로드 성공")
    void downloadBinaryContent_Success() throws Exception {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "download-test.txt",
                "text/plain",
                "download test content".getBytes()
        );
        BinaryContent saved = binaryContentService.createBinaryContent(file);

        // when & then
        mockMvc.perform(get("/api/binaryContents/{binaryContentId}/download", saved.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("존재하지 않는 파일 다운로드 시 예외 발생")
    void downloadBinaryContent_NotFound() throws Exception {
        // given
        UUID nonExistentId = UUID.randomUUID();

        // when & then
        mockMvc.perform(get("/api/binaryContents/{binaryContentId}/download", nonExistentId))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("파일 삭제 성공")
    void deleteBinaryContent_Success() throws Exception {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "to-delete.txt",
                "text/plain",
                "delete me".getBytes()
        );
        BinaryContent saved = binaryContentService.createBinaryContent(file);

        // when & then
        mockMvc.perform(delete("/api/binaryContents/{binaryContentId}", saved.getId()))
                .andExpect(status().isOk());

        // verify
        mockMvc.perform(get("/api/binaryContents/findUrl/{id}", saved.getId()))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("존재하지 않는 파일 삭제 시 예외 발생")
    void deleteBinaryContent_NotFound() throws Exception {
        // given
        UUID nonExistentId = UUID.randomUUID();

        // when & then
        mockMvc.perform(delete("/api/binaryContents/{binaryContentId}", nonExistentId))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("대용량 파일 업로드")
    void saveLargeFile_Success() throws Exception {
        // given
        byte[] largeContent = new byte[5 * 1024 * 1024]; // 5MB
        MockMultipartFile largeFile = new MockMultipartFile(
                "multipartFile",
                "large-video.mp4",
                "video/mp4",
                largeContent
        );

        // when & then
        mockMvc.perform(multipart("/api/binaryContents")
                        .file(largeFile))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fileName").value("large-video.mp4"))
                .andExpect(jsonPath("$.contentType").value("video/mp4"))
                .andExpect(jsonPath("$.size").value(largeContent.length));
    }

    @Test
    @DisplayName("다양한 파일 타입 업로드")
    void saveVariousFileTypes_Success() throws Exception {
        // given - Image
        MockMultipartFile imageFile = new MockMultipartFile(
                "multipartFile",
                "photo.jpg",
                "image/jpeg",
                "image content".getBytes()
        );

        // when & then
        mockMvc.perform(multipart("/api/binaryContents")
                        .file(imageFile))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fileName").value("photo.jpg"))
                .andExpect(jsonPath("$.contentType").value("image/jpeg"));

        // given - PDF
        MockMultipartFile pdfFile = new MockMultipartFile(
                "multipartFile",
                "document.pdf",
                "application/pdf",
                "pdf content".getBytes()
        );

        // when & then
        mockMvc.perform(multipart("/api/binaryContents")
                        .file(pdfFile))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fileName").value("document.pdf"))
                .andExpect(jsonPath("$.contentType").value("application/pdf"));

        // given - Video
        MockMultipartFile videoFile = new MockMultipartFile(
                "multipartFile",
                "clip.mp4",
                "video/mp4",
                "video content".getBytes()
        );

        // when & then
        mockMvc.perform(multipart("/api/binaryContents")
                        .file(videoFile))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fileName").value("clip.mp4"))
                .andExpect(jsonPath("$.contentType").value("video/mp4"));
    }

    @Test
    @DisplayName("빈 파일 업로드 시 null 반환")
    void saveEmptyFile_ReturnsNull() throws Exception {
        // given
        MockMultipartFile emptyFile = new MockMultipartFile(
                "multipartFile",
                "empty.txt",
                "text/plain",
                new byte[0]
        );

        // when & then
        mockMvc.perform(multipart("/api/binaryContents")
                        .file(emptyFile))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("파일 업로드 후 조회 가능")
    void uploadAndRetrieve_Success() throws Exception {
        // given - upload
        MockMultipartFile file = new MockMultipartFile(
                "multipartFile",
                "upload-retrieve.txt",
                "text/plain",
                "test content for retrieval".getBytes()
        );

        String response = mockMvc.perform(multipart("/api/binaryContents")
                        .file(file))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract ID from response (simple parsing)
        String idStr = response.split("\"id\":\"")[1].split("\"")[0];
        UUID uploadedId = UUID.fromString(idStr);

        // when & then - retrieve URL
        mockMvc.perform(get("/api/binaryContents/findUrl/{id}", uploadedId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(uploadedId.toString())));

        // when & then - download
        mockMvc.perform(get("/api/binaryContents/{binaryContentId}/download", uploadedId))
                .andExpect(status().isOk());
    }
}
