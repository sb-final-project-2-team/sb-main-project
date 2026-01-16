package com.codeit.closet.module.binarycontent.service;

import com.codeit.closet.module.binarycontent.entity.BinaryContent;
import com.codeit.closet.module.binarycontent.repository.BinaryContentRepository;
import com.codeit.closet.module.binarycontent.service.impl.BasicBinaryContentService;
import com.codeit.closet.module.binarycontent.storage.BinaryContentStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BasicBinaryContentService 테스트")
class BasicBinaryContentServiceTest {

    @Mock
    private BinaryContentRepository binaryContentRepository;

    @Mock
    private BinaryContentStorage binaryContentStorage;

    @InjectMocks
    private BasicBinaryContentService binaryContentService;

    private UUID testBinaryContentId;
    private BinaryContent testBinaryContent;
    private MultipartFile mockFile;

    @BeforeEach
    void setUp() {
        testBinaryContentId = UUID.randomUUID();

        testBinaryContent = BinaryContent.builder()
                .fileName("test-image.jpg")
                .fileUrl("https://closet-s3-bucket.s3.ap-northeast-2.amazonaws.com/" + testBinaryContentId)
                .size(1024L)
                .contentType("image/jpeg")
                .build();

        mockFile = mock(MultipartFile.class);
    }

    @Test
    @DisplayName("바이너리 콘텐츠 생성 성공")
    void createBinaryContent_Success() throws IOException {
        // given
        when(mockFile.getOriginalFilename()).thenReturn("test-image.jpg");
        when(mockFile.getContentType()).thenReturn("image/jpeg");
        when(mockFile.getSize()).thenReturn(1024L);
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getBytes()).thenReturn(new byte[1024]);

        BinaryContent savedContent = BinaryContent.builder()
                .fileName("test-image.jpg")
                .contentType("image/jpeg")
                .size(1024L)
                .build();

        // Use
        BinaryContent savedWithId = spy(savedContent);
        when(savedWithId.getId()).thenReturn(testBinaryContentId);

        when(binaryContentRepository.save(any(BinaryContent.class))).thenReturn(savedWithId);
        when(binaryContentStorage.save(eq(testBinaryContentId), any(byte[].class))).thenReturn(testBinaryContentId);

        // when
        BinaryContent result = binaryContentService.createBinaryContent(mockFile);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getFileName()).isEqualTo("test-image.jpg");
        assertThat(result.getContentType()).isEqualTo("image/jpeg");
        assertThat(result.getSize()).isEqualTo(1024L);

        verify(binaryContentRepository, times(1)).save(any(BinaryContent.class));
        verify(binaryContentStorage, times(1)).save(eq(testBinaryContentId), any(byte[].class));
    }

    @Test
    @DisplayName("null 파일로 바이너리 콘텐츠 생성 시 null 반환")
    void createBinaryContent_NullFile() {
        // when
        BinaryContent result = binaryContentService.createBinaryContent(null);

        // then
        assertThat(result).isNull();
        verify(binaryContentRepository, never()).save(any(BinaryContent.class));
        verify(binaryContentStorage, never()).save(any(UUID.class), any(byte[].class));
    }

    @Test
    @DisplayName("빈 파일로 바이너리 콘텐츠 생성 시 null 반환")
    void createBinaryContent_EmptyFile() {
        // given
        when(mockFile.isEmpty()).thenReturn(true);

        // when
        BinaryContent result = binaryContentService.createBinaryContent(mockFile);

        // then
        assertThat(result).isNull();
        verify(binaryContentRepository, never()).save(any(BinaryContent.class));
        verify(binaryContentStorage, never()).save(any(UUID.class), any(byte[].class));
    }

    @Test
    @DisplayName("스토리지 저장 실패 시 RuntimeException 발생")
    void createBinaryContent_StorageException() throws IOException {
        // given
        when(mockFile.getOriginalFilename()).thenReturn("test-image.jpg");
        when(mockFile.getContentType()).thenReturn("image/jpeg");
        when(mockFile.getSize()).thenReturn(1024L);
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getBytes()).thenThrow(new IOException("Storage error"));

        BinaryContent savedWithId = spy(BinaryContent.builder()
                .fileName("test-image.jpg")
                .contentType("image/jpeg")
                .size(1024L)
                .build());
        when(savedWithId.getId()).thenReturn(testBinaryContentId);

        when(binaryContentRepository.save(any(BinaryContent.class))).thenReturn(savedWithId);

        // when & then
        assertThatThrownBy(() -> binaryContentService.createBinaryContent(mockFile))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Storage error");

        verify(binaryContentRepository, times(1)).save(any(BinaryContent.class));
    }

    @Test
    @DisplayName("바이너리 콘텐츠 ID로 파일 URL 조회 성공")
    void findFileUrlByBinaryContentId_Success() {
        // given
        when(binaryContentRepository.findById(testBinaryContentId))
                .thenReturn(Optional.of(testBinaryContent));

        // when
        String fileUrl = binaryContentService.findFileUrlByBinaryContentId(testBinaryContentId);

        // then
        assertThat(fileUrl).isNotNull();
        assertThat(fileUrl).isEqualTo(testBinaryContent.getFileUrl());
        verify(binaryContentRepository, times(1)).findById(testBinaryContentId);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 파일 URL 조회 시 예외 발생")
    void findFileUrlByBinaryContentId_NotFound() {
        // given
        UUID nonExistentId = UUID.randomUUID();
        when(binaryContentRepository.findById(nonExistentId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> binaryContentService.findFileUrlByBinaryContentId(nonExistentId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("BinaryContent를 찾을 수 없습니다");

        verify(binaryContentRepository, times(1)).findById(nonExistentId);
    }

    @Test
    @DisplayName("바이너리 콘텐츠 ID로 엔티티 조회 성공")
    void findByBinaryContentId_Success() {
        // given
        when(binaryContentRepository.findById(testBinaryContentId))
                .thenReturn(Optional.of(testBinaryContent));

        // when
        BinaryContent result = binaryContentService.findByBinaryContentId(testBinaryContentId);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(testBinaryContent);
        assertThat(result.getFileName()).isEqualTo("test-image.jpg");
        verify(binaryContentRepository, times(1)).findById(testBinaryContentId);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 엔티티 조회 시 예외 발생")
    void findByBinaryContentId_NotFound() {
        // given
        UUID nonExistentId = UUID.randomUUID();
        when(binaryContentRepository.findById(nonExistentId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> binaryContentService.findByBinaryContentId(nonExistentId))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("존재하지않는 binaryContent 입니다");

        verify(binaryContentRepository, times(1)).findById(nonExistentId);
    }

    @Test
    @DisplayName("바이너리 콘텐츠 삭제 성공")
    void deleteBinaryContent_Success() {
        // given
        when(binaryContentRepository.existsById(testBinaryContentId)).thenReturn(true);

        // when
        binaryContentService.deleteBinaryContent(testBinaryContentId);

        // then
        verify(binaryContentRepository, times(1)).existsById(testBinaryContentId);
        verify(binaryContentRepository, times(1)).deleteById(testBinaryContentId);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 삭제 시 예외 발생")
    void deleteBinaryContent_NotFound() {
        // given
        UUID nonExistentId = UUID.randomUUID();
        when(binaryContentRepository.existsById(nonExistentId)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> binaryContentService.deleteBinaryContent(nonExistentId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("BinaryContent를 찾을 수 없습니다");

        verify(binaryContentRepository, times(1)).existsById(nonExistentId);
        verify(binaryContentRepository, never()).deleteById(nonExistentId);
    }

    @Test
    @DisplayName("다양한 파일 타입 업로드 테스트")
    void createBinaryContent_VariousFileTypes() throws IOException {
        // given - PDF file
        UUID pdfId = UUID.randomUUID();
        MultipartFile pdfFile = mock(MultipartFile.class);
        when(pdfFile.getOriginalFilename()).thenReturn("document.pdf");
        when(pdfFile.getContentType()).thenReturn("application/pdf");
        when(pdfFile.getSize()).thenReturn(2048L);
        when(pdfFile.isEmpty()).thenReturn(false);
        when(pdfFile.getBytes()).thenReturn(new byte[2048]);

        BinaryContent savedPdf = spy(BinaryContent.builder()
                .fileName("document.pdf")
                .contentType("application/pdf")
                .size(2048L)
                .build());
        when(savedPdf.getId()).thenReturn(pdfId);

        when(binaryContentRepository.save(any(BinaryContent.class))).thenReturn(savedPdf);
        when(binaryContentStorage.save(eq(pdfId), any(byte[].class))).thenReturn(pdfId);

        // when
        BinaryContent result = binaryContentService.createBinaryContent(pdfFile);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContentType()).isEqualTo("application/pdf");
        assertThat(result.getFileName()).isEqualTo("document.pdf");
    }

    @Test
    @DisplayName("대용량 파일 업로드 테스트")
    void createBinaryContent_LargeFile() throws IOException {
        // given
        long largeFileSize = 10 * 1024 * 1024L; // 10MB
        when(mockFile.getOriginalFilename()).thenReturn("large-video.mp4");
        when(mockFile.getContentType()).thenReturn("video/mp4");
        when(mockFile.getSize()).thenReturn(largeFileSize);
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getBytes()).thenReturn(new byte[(int) largeFileSize]);

        BinaryContent savedContent = spy(BinaryContent.builder()
                .fileName("large-video.mp4")
                .contentType("video/mp4")
                .size(largeFileSize)
                .build());
        when(savedContent.getId()).thenReturn(testBinaryContentId);

        when(binaryContentRepository.save(any(BinaryContent.class))).thenReturn(savedContent);
        when(binaryContentStorage.save(any(UUID.class), any(byte[].class))).thenReturn(testBinaryContentId);

        // when
        BinaryContent result = binaryContentService.createBinaryContent(mockFile);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getSize()).isEqualTo(largeFileSize);
        assertThat(result.getContentType()).isEqualTo("video/mp4");
    }
}
