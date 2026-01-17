package com.codeit.closet.module.binarycontent.storage.local;

import com.codeit.closet.module.binarycontent.entity.BinaryContent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("LocalBinaryContentStorage 테스트")
class LocalBinaryContentStorageTest {

    private LocalBinaryContentStorage storage;
    private Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("test-storage");
        storage = new LocalBinaryContentStorage(tempDir);
        storage.init();
    }

    @AfterEach
    void tearDown() throws IOException {
        if (Files.exists(tempDir)) {
            Files.walk(tempDir)
                    .sorted((a, b) -> b.compareTo(a))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            // ignore
                        }
                    });
        }
    }

    @Test
    @DisplayName("파일 저장 성공")
    void save_Success() {
        // given
        UUID fileId = UUID.randomUUID();
        byte[] content = "test file content".getBytes();

        // when
        UUID result = storage.save(fileId, content);

        // then
        assertThat(result).isEqualTo(fileId);
        assertThat(Files.exists(tempDir.resolve(fileId.toString()))).isTrue();
    }

    @Test
    @DisplayName("중복된 파일 저장 시 예외 발생")
    void save_DuplicateFile() {
        // given
        UUID fileId = UUID.randomUUID();
        byte[] content = "test file content".getBytes();
        storage.save(fileId, content);

        // when & then
        assertThatThrownBy(() -> storage.save(fileId, content))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("파일 조회 성공")
    void get_Success() throws IOException {
        // given
        UUID fileId = UUID.randomUUID();
        byte[] content = "test file content".getBytes();
        storage.save(fileId, content);

        // when
        InputStream inputStream = storage.get(fileId);

        // then
        assertThat(inputStream).isNotNull();
        byte[] readContent = inputStream.readAllBytes();
        assertThat(readContent).isEqualTo(content);
        inputStream.close();
    }

    @Test
    @DisplayName("존재하지 않는 파일 조회 시 예외 발생")
    void get_FileNotFound() {
        // given
        UUID nonExistentId = UUID.randomUUID();

        // when & then
        assertThatThrownBy(() -> storage.get(nonExistentId))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("does not exist");
    }

    @Test
    @DisplayName("파일 다운로드 성공")
    void download_Success() throws IOException {
        // given
        UUID fileId = UUID.randomUUID();
        byte[] content = "download test content".getBytes();
        storage.save(fileId, content);

        BinaryContent metadata = BinaryContent.builder()
                .id(fileId)
                .fileName("test-file.txt")
                .contentType("text/plain")
                .size((long) content.length)
                .build();

        // when
        ResponseEntity<Resource> response = storage.download(metadata);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .contains("attachment")
                .contains("test-file.txt");
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE))
                .isEqualTo("text/plain");
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_LENGTH))
                .isEqualTo(String.valueOf(content.length));
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("대용량 파일 저장 및 조회")
    void saveLargeFile_Success() throws IOException {
        // given
        UUID fileId = UUID.randomUUID();
        byte[] largeContent = new byte[5 * 1024 * 1024]; // 5MB
        for (int i = 0; i < largeContent.length; i++) {
            largeContent[i] = (byte) (i % 256);
        }

        // when
        storage.save(fileId, largeContent);
        InputStream inputStream = storage.get(fileId);

        // then
        byte[] readContent = inputStream.readAllBytes();
        assertThat(readContent).hasSize(largeContent.length);
        assertThat(readContent).isEqualTo(largeContent);
        inputStream.close();
    }

    @Test
    @DisplayName("여러 파일 저장 및 조회")
    void saveMultipleFiles_Success() throws IOException {
        // given
        UUID file1Id = UUID.randomUUID();
        UUID file2Id = UUID.randomUUID();
        UUID file3Id = UUID.randomUUID();

        byte[] content1 = "file 1 content".getBytes();
        byte[] content2 = "file 2 content".getBytes();
        byte[] content3 = "file 3 content".getBytes();

        // when
        storage.save(file1Id, content1);
        storage.save(file2Id, content2);
        storage.save(file3Id, content3);

        // then
        InputStream is1 = storage.get(file1Id);
        InputStream is2 = storage.get(file2Id);
        InputStream is3 = storage.get(file3Id);

        assertThat(is1.readAllBytes()).isEqualTo(content1);
        assertThat(is2.readAllBytes()).isEqualTo(content2);
        assertThat(is3.readAllBytes()).isEqualTo(content3);

        is1.close();
        is2.close();
        is3.close();
    }

    @Test
    @DisplayName("빈 파일 저장 및 조회")
    void saveEmptyFile_Success() throws IOException {
        // given
        UUID fileId = UUID.randomUUID();
        byte[] emptyContent = new byte[0];

        // when
        storage.save(fileId, emptyContent);
        InputStream inputStream = storage.get(fileId);

        // then
        byte[] readContent = inputStream.readAllBytes();
        assertThat(readContent).isEmpty();
        inputStream.close();
    }

    @Test
    @DisplayName("저장 디렉토리가 없을 때 자동 생성")
    void init_CreateDirectory() throws IOException {
        // given
        Path newTempDir = Files.createTempDirectory("test-new-storage");
        Files.delete(newTempDir); // 디렉토리 삭제

        // when
        LocalBinaryContentStorage newStorage = new LocalBinaryContentStorage(newTempDir);
        newStorage.init();

        // then
        assertThat(Files.exists(newTempDir)).isTrue();
        assertThat(Files.isDirectory(newTempDir)).isTrue();

        // cleanup
        Files.deleteIfExists(newTempDir);
    }

    @Test
    @DisplayName("다양한 파일 타입 저장")
    void saveVariousFileTypes_Success() throws IOException {
        // given
        UUID imageId = UUID.randomUUID();
        UUID pdfId = UUID.randomUUID();
        UUID videoId = UUID.randomUUID();

        byte[] imageContent = "fake image binary".getBytes();
        byte[] pdfContent = "fake pdf binary".getBytes();
        byte[] videoContent = "fake video binary".getBytes();

        // when
        storage.save(imageId, imageContent);
        storage.save(pdfId, pdfContent);
        storage.save(videoId, videoContent);

        // then
        assertThat(storage.get(imageId).readAllBytes()).isEqualTo(imageContent);
        assertThat(storage.get(pdfId).readAllBytes()).isEqualTo(pdfContent);
        assertThat(storage.get(videoId).readAllBytes()).isEqualTo(videoContent);
    }
}
