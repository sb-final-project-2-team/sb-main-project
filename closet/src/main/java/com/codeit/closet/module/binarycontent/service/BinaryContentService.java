package com.codeit.closet.module.binarycontent.service;

import com.codeit.closet.module.binarycontent.entity.BinaryContent;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface BinaryContentService {

  BinaryContent createBinaryContent(MultipartFile multipartFile);

  String findFileUrlByBinaryContentId(UUID binaryContentId);

  void deleteBinaryContent(UUID binaryContentId);

  BinaryContent findByBinaryContentId(UUID binaryContentId);
}
