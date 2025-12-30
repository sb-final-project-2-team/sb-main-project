package com.codeit.closet.module.binarycontent.service.impl;

import com.codeit.closet.module.binarycontent.entity.BinaryContent;
import com.codeit.closet.module.binarycontent.repository.BinaryContentRepository;
import com.codeit.closet.module.binarycontent.service.BinaryContentService;
import com.codeit.closet.module.binarycontent.storage.BinaryContentStorage;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class BasicBinaryContentService implements BinaryContentService {

  private final BinaryContentRepository binaryContentRepository;
  private final BinaryContentStorage binaryContentStorage;

  @Override
  @Transactional
  public BinaryContent createBinaryContent(MultipartFile multipartFile) {

    if (multipartFile == null || multipartFile.isEmpty()) {
      return null;
    }
    BinaryContent binaryContent = BinaryContent.builder()
        .fileName(multipartFile.getOriginalFilename())
        .contentType(multipartFile.getContentType())
        .size(multipartFile.getSize())
        .build();

    BinaryContent saved = binaryContentRepository.save(binaryContent);

    try {
      binaryContentStorage.save(saved.getId(), multipartFile.getBytes());
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage());
    }

    return saved;
  }

  @Override
  @Transactional(readOnly = true)
  public String findFileUrlByBinaryContentId(UUID binaryContentId) {
    BinaryContent binaryContent = binaryContentRepository.findById(binaryContentId).orElseThrow(
        () -> new IllegalArgumentException("BinaryContent를 찾을 수 없습니다. : " + binaryContentId)
    );

    return binaryContent.getFileUrl();
  }

  @Override // 요구사항에 없는 임시
  @Transactional(readOnly = true)
  public BinaryContent findByBinaryContentId(UUID binaryContentId) {
    return binaryContentRepository.findById(binaryContentId).orElseThrow(
        () -> new NoSuchElementException("존재하지않는 binaryContent 입니다.")
    );
  }

  @Override // 요구사항에 없는 임시
  @Transactional
  public void deleteBinaryContent(UUID binaryContentId) {
    if (!binaryContentRepository.existsById(binaryContentId)) {
      throw new IllegalArgumentException("BinaryContent를 찾을 수 없습니다. : " + binaryContentId);
    }

    binaryContentRepository.deleteById(binaryContentId);
  }
}
