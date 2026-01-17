package com.codeit.closet.module.binarycontent.storage.s3;

import com.codeit.closet.module.binarycontent.entity.BinaryContent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("S3BinaryContentStorage 테스트")
class S3BinaryContentStorageTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Presigner presigner;

    private S3BinaryContentStorage storage;
    private static final String TEST_BUCKET = "test-bucket";
    private static final long PRESIGNED_URL_EXPIRATION = 600L;

    @BeforeEach
    void setUp() {
        storage = spy(new S3BinaryContentStorage(
                "test-access-key",
                "test-secret-key",
                "ap-northeast-2",
                TEST_BUCKET
        ));

        ReflectionTestUtils.setField(storage, "s3Client", s3Client);
        ReflectionTestUtils.setField(storage, "presigner", presigner);
        ReflectionTestUtils.setField(storage, "presignedUrlExpiration", PRESIGNED_URL_EXPIRATION);
    }

    @Test
    @DisplayName("파일 저장 성공")
    void save_Success() {
        // given
        UUID fileId = UUID.randomUUID();
        byte[] content = "test file content".getBytes();

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        // when
        UUID result = storage.save(fileId, content);

        // then
        assertThat(result).isEqualTo(fileId);

        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client, times(1)).putObject(requestCaptor.capture(), any(RequestBody.class));

        PutObjectRequest capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest.bucket()).isEqualTo(TEST_BUCKET);
        assertThat(capturedRequest.key()).isEqualTo(fileId.toString());
    }

    @Test
    @DisplayName("파일 저장 실패 시 RuntimeException 발생")
    void save_Failure() {
        // given
        UUID fileId = UUID.randomUUID();
        byte[] content = "test file content".getBytes();

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(new RuntimeException("S3 upload failed"));

        // when & then
        assertThatThrownBy(() -> storage.save(fileId, content))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("S3 upload failed");
    }

    @Test
    @DisplayName("파일 조회 성공")
    void get_Success() throws IOException {
        // given
        UUID fileId = UUID.randomUUID();
        byte[] content = "test file content".getBytes();

        @SuppressWarnings("unchecked")
        ResponseInputStream<GetObjectResponse> mockResponseStream = mock(ResponseInputStream.class);
        when(mockResponseStream.readAllBytes()).thenReturn(content);

        when(s3Client.getObject(any(GetObjectRequest.class)))
                .thenReturn(mockResponseStream);

        // when
        InputStream result = storage.get(fileId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.readAllBytes()).isEqualTo(content);

        ArgumentCaptor<GetObjectRequest> requestCaptor = ArgumentCaptor.forClass(GetObjectRequest.class);
        verify(s3Client, times(1)).getObject(requestCaptor.capture());

        GetObjectRequest capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest.bucket()).isEqualTo(TEST_BUCKET);
        assertThat(capturedRequest.key()).isEqualTo(fileId.toString());
    }

    @Test
    @DisplayName("존재하지 않는 파일 조회 시 NoSuchElementException 발생")
    void get_FileNotFound() {
        // given
        UUID nonExistentId = UUID.randomUUID();

        when(s3Client.getObject(any(GetObjectRequest.class)))
                .thenThrow(NoSuchKeyException.builder().message("Key not found").build());

        // when & then
        assertThatThrownBy(() -> storage.get(nonExistentId))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("does not exist in S3");
    }

    @Test
    @DisplayName("S3 조회 실패 시 RuntimeException 발생")
    void get_S3Exception() {
        // given
        UUID fileId = UUID.randomUUID();

        when(s3Client.getObject(any(GetObjectRequest.class)))
                .thenThrow(new RuntimeException("S3 connection failed"));

        // when & then
        assertThatThrownBy(() -> storage.get(fileId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to get object from S3");
    }

    @Test
    @DisplayName("파일 다운로드 성공 - Presigned URL 리다이렉트")
    void download_Success() throws MalformedURLException {
        // given
        UUID fileId = UUID.randomUUID();
        BinaryContent metadata = BinaryContent.builder()
                .id(fileId)
                .fileName("test-file.txt")
                .contentType("text/plain")
                .size(1024L)
                .build();

        String presignedUrl = "https://test-bucket.s3.amazonaws.com/presigned-url";
        doReturn(presignedUrl).when(storage).generatePresignedUrl(anyString(), anyString());

        // when
        ResponseEntity<Resource> response = storage.download(metadata);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(response.getHeaders().getFirst(HttpHeaders.LOCATION)).isEqualTo(presignedUrl);

        verify(storage, times(1)).generatePresignedUrl(fileId.toString(), metadata.getContentType());
    }

    @Test
    @DisplayName("Presigned URL 생성 성공")
    void generatePresignedUrl_Success() throws MalformedURLException {
        // given
        String key = UUID.randomUUID().toString();
        String contentType = "application/pdf";
        String expectedUrl = "https://test-bucket.s3.amazonaws.com/presigned?signature=xyz";

        PresignedGetObjectRequest mockPresignedRequest = mock(PresignedGetObjectRequest.class);
        when(mockPresignedRequest.url()).thenReturn(new URL(expectedUrl));

        when(presigner.presignGetObject(any(GetObjectPresignRequest.class)))
                .thenReturn(mockPresignedRequest);

        // Create a real instance for this test
        S3BinaryContentStorage realStorage = new S3BinaryContentStorage(
                "test-access-key",
                "test-secret-key",
                "ap-northeast-2",
                TEST_BUCKET
        );
        ReflectionTestUtils.setField(realStorage, "presigner", presigner);
        ReflectionTestUtils.setField(realStorage, "presignedUrlExpiration", PRESIGNED_URL_EXPIRATION);

        // when
        String result = realStorage.generatePresignedUrl(key, contentType);

        // then
        assertThat(result).isEqualTo(expectedUrl);

        ArgumentCaptor<GetObjectPresignRequest> requestCaptor = ArgumentCaptor.forClass(GetObjectPresignRequest.class);
        verify(presigner, times(1)).presignGetObject(requestCaptor.capture());

        GetObjectPresignRequest capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest.getObjectRequest().bucket()).isEqualTo(TEST_BUCKET);
        assertThat(capturedRequest.getObjectRequest().key()).isEqualTo(key);
        assertThat(capturedRequest.getObjectRequest().responseContentType()).isEqualTo(contentType);
    }

    @Test
    @DisplayName("대용량 파일 저장")
    void saveLargeFile_Success() {
        // given
        UUID fileId = UUID.randomUUID();
        byte[] largeContent = new byte[10 * 1024 * 1024]; // 10MB

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        // when
        UUID result = storage.save(fileId, largeContent);

        // then
        assertThat(result).isEqualTo(fileId);
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    @DisplayName("리소스 정리 테스트")
    void resourceCleanup_Success() {
        // when
        storage.resourceCleanup();

        // then
        verify(s3Client, times(1)).close();
        verify(presigner, times(1)).close();
    }

    @Test
    @DisplayName("다양한 파일 타입 저장")
    void saveVariousFileTypes_Success() {
        // given
        UUID imageId = UUID.randomUUID();
        UUID pdfId = UUID.randomUUID();
        UUID videoId = UUID.randomUUID();

        byte[] imageContent = "fake image binary".getBytes();
        byte[] pdfContent = "fake pdf binary".getBytes();
        byte[] videoContent = "fake video binary".getBytes();

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        // when
        UUID imageResult = storage.save(imageId, imageContent);
        UUID pdfResult = storage.save(pdfId, pdfContent);
        UUID videoResult = storage.save(videoId, videoContent);

        // then
        assertThat(imageResult).isEqualTo(imageId);
        assertThat(pdfResult).isEqualTo(pdfId);
        assertThat(videoResult).isEqualTo(videoId);

        verify(s3Client, times(3)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    @DisplayName("S3Client getter 테스트")
    void getS3Client_Success() {
        // when
        S3Client result = storage.getS3Client();

        // then
        assertThat(result).isEqualTo(s3Client);
    }
}
