package com.codeit.closet.module.binarycontent.repository;

import com.codeit.closet.module.binarycontent.entity.BinaryContent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@DisplayName("BinaryContentRepository 테스트")
class BinaryContentRepositoryTest {

    @Autowired
    private BinaryContentRepository binaryContentRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    @Test
    @DisplayName("바이너리 콘텐츠 저장 성공")
    void saveBinaryContent_Success() {
        // given
        BinaryContent binaryContent = BinaryContent.builder()
                .fileName("test-image.jpg")
                .fileUrl("https://example.com/test-image.jpg")
                .size(1024L)
                .contentType("image/jpeg")
                .build();

        // when
        BinaryContent saved = binaryContentRepository.save(binaryContent);
        entityManager.flush();

        // then
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getFileName()).isEqualTo("test-image.jpg");
        assertThat(saved.getFileUrl()).isEqualTo("https://example.com/test-image.jpg");
        assertThat(saved.getSize()).isEqualTo(1024L);
        assertThat(saved.getContentType()).isEqualTo("image/jpeg");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("ID로 바이너리 콘텐츠 조회 성공")
    void findById_Success() {
        // given
        BinaryContent binaryContent = BinaryContent.builder()
                .fileName("document.pdf")
                .fileUrl("https://example.com/document.pdf")
                .size(2048L)
                .contentType("application/pdf")
                .build();
        BinaryContent saved = binaryContentRepository.save(binaryContent);
        entityManager.flush();
        entityManager.clear();

        // when
        Optional<BinaryContent> found = binaryContentRepository.findById(saved.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
        assertThat(found.get().getFileName()).isEqualTo("document.pdf");
        assertThat(found.get().getContentType()).isEqualTo("application/pdf");
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회 시 빈 Optional 반환")
    void findById_NotFound() {
        // given
        UUID nonExistentId = UUID.randomUUID();

        // when
        Optional<BinaryContent> found = binaryContentRepository.findById(nonExistentId);

        // then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("바이너리 콘텐츠 삭제 성공")
    void deleteBinaryContent_Success() {
        // given
        BinaryContent binaryContent = BinaryContent.builder()
                .fileName("to-delete.txt")
                .fileUrl("https://example.com/to-delete.txt")
                .size(512L)
                .contentType("text/plain")
                .build();
        BinaryContent saved = binaryContentRepository.save(binaryContent);
        entityManager.flush();

        // when
        binaryContentRepository.deleteById(saved.getId());
        entityManager.flush();

        // then
        Optional<BinaryContent> found = binaryContentRepository.findById(saved.getId());
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("모든 바이너리 콘텐츠 조회")
    void findAll_Success() {
        // given
        BinaryContent content1 = BinaryContent.builder()
                .fileName("file1.jpg")
                .fileUrl("https://example.com/file1.jpg")
                .size(1024L)
                .contentType("image/jpeg")
                .build();
        BinaryContent content2 = BinaryContent.builder()
                .fileName("file2.png")
                .fileUrl("https://example.com/file2.png")
                .size(2048L)
                .contentType("image/png")
                .build();
        binaryContentRepository.save(content1);
        binaryContentRepository.save(content2);
        entityManager.flush();

        // when
        List<BinaryContent> all = binaryContentRepository.findAll();

        // then
        assertThat(all).hasSizeGreaterThanOrEqualTo(2);
        assertThat(all).extracting("fileName")
                .contains("file1.jpg", "file2.png");
    }

    @Test
    @DisplayName("바이너리 콘텐츠 존재 여부 확인 - 존재하는 경우")
    void existsById_True() {
        // given
        BinaryContent binaryContent = BinaryContent.builder()
                .fileName("exists.txt")
                .fileUrl("https://example.com/exists.txt")
                .size(128L)
                .contentType("text/plain")
                .build();
        BinaryContent saved = binaryContentRepository.save(binaryContent);
        entityManager.flush();

        // when
        boolean exists = binaryContentRepository.existsById(saved.getId());

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("바이너리 콘텐츠 존재 여부 확인 - 존재하지 않는 경우")
    void existsById_False() {
        // given
        UUID nonExistentId = UUID.randomUUID();

        // when
        boolean exists = binaryContentRepository.existsById(nonExistentId);

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("바이너리 콘텐츠 개수 조회")
    void count_Success() {
        // given
        long initialCount = binaryContentRepository.count();

        BinaryContent content1 = BinaryContent.builder()
                .fileName("count1.txt")
                .fileUrl("https://example.com/count1.txt")
                .size(100L)
                .contentType("text/plain")
                .build();
        BinaryContent content2 = BinaryContent.builder()
                .fileName("count2.txt")
                .fileUrl("https://example.com/count2.txt")
                .size(200L)
                .contentType("text/plain")
                .build();
        binaryContentRepository.save(content1);
        binaryContentRepository.save(content2);
        entityManager.flush();

        // when
        long count = binaryContentRepository.count();

        // then
        assertThat(count).isEqualTo(initialCount + 2);
    }

    @Test
    @DisplayName("fileUrl이 null일 때 PrePersist로 자동 생성")
    void prePersist_AutoGenerateFileUrl() {
        // given
        BinaryContent binaryContent = BinaryContent.builder()
                .fileName("auto-url.jpg")
                .size(1024L)
                .contentType("image/jpeg")
                .build();

        // when
        BinaryContent saved = binaryContentRepository.save(binaryContent);
        entityManager.flush();
        entityManager.clear();

        // then
        BinaryContent found = binaryContentRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getFileUrl()).isNotNull();
        assertThat(found.getFileUrl()).contains(found.getId().toString());
        assertThat(found.getFileUrl()).startsWith("https://closet-s3-bucket.s3.ap-northeast-2.amazonaws.com/");
    }

    @Test
    @DisplayName("다양한 파일 타입 저장 테스트")
    void saveVariousContentTypes_Success() {
        // given & when
        BinaryContent image = binaryContentRepository.save(BinaryContent.builder()
                .fileName("photo.jpg")
                .fileUrl("https://example.com/photo.jpg")
                .size(5000L)
                .contentType("image/jpeg")
                .build());

        BinaryContent video = binaryContentRepository.save(BinaryContent.builder()
                .fileName("video.mp4")
                .fileUrl("https://example.com/video.mp4")
                .size(50000L)
                .contentType("video/mp4")
                .build());

        BinaryContent document = binaryContentRepository.save(BinaryContent.builder()
                .fileName("report.pdf")
                .fileUrl("https://example.com/report.pdf")
                .size(10000L)
                .contentType("application/pdf")
                .build());

        entityManager.flush();

        // then
        assertThat(image.getContentType()).isEqualTo("image/jpeg");
        assertThat(video.getContentType()).isEqualTo("video/mp4");
        assertThat(document.getContentType()).isEqualTo("application/pdf");
    }
}
