package com.codeit.closet.module.binarycontent.storage;

import com.codeit.closet.module.binarycontent.entity.BinaryContent;
import java.io.InputStream;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

public interface BinaryContentStorage {
  UUID save(UUID id, byte[] data);

  InputStream get(UUID id);

  ResponseEntity<Resource> download(BinaryContent binaryContent);
}
