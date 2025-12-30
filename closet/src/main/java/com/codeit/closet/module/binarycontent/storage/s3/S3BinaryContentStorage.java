package com.codeit.closet.module.binarycontent.storage.s3;

import com.codeit.closet.module.binarycontent.entity.BinaryContent;
import com.codeit.closet.module.binarycontent.storage.BinaryContentStorage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

// 뼈대만 생성
public class S3BinaryContentStorage implements BinaryContentStorage {

  private final Path root;

  public S3BinaryContentStorage(@Value("${closet.storage.local.root-path") Path root) {
    this.root = root;
  }

  //받는 ㅇ"https://sprint-sb-project.s3.ap-northeast-2.amazonaws.com/otboo/images/a0cea2e2-fd5a-43ad-aa0f-42a008927812?response-content-type=image%2Fjpeg&X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20251230T053543Z&X-Amz-SignedHeaders=host&X-Amz-Credential=AKIAQ67VTHC26F6IWSAU%2F20251230%2Fap-northeast-2%2Fs3%2Faws4_request&X-Amz-Expires=3600&X-Amz-Signature=f5bb0be8045c5b694fab9449c0d86e7b88bfcaa3a3bd2f23d1894af67092d981"

  public UUID save(UUID id, byte[] bytes) {
    Path filePath = resolvePath(id);
    if (Files.exists(filePath)) {
      throw new IllegalArgumentException("File with key " + id + " already exists");
    }
    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Thread interrupted while simulating delay", e);
    }
    try (OutputStream outputStream = Files.newOutputStream(filePath)) {
      outputStream.write(bytes);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return id;
  }

  public InputStream get(UUID binaryContentId) {
    Path filePath = resolvePath(binaryContentId);
    if (Files.notExists(filePath)) {
      throw new NoSuchElementException("File with key " + binaryContentId + " does not exist");
    }
    try {
      return Files.newInputStream(filePath);
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  @Override
  public ResponseEntity<Resource> download(BinaryContent metaData) {
    InputStream inputStream = get(metaData.getId());
    Resource resource = new InputStreamResource(inputStream);

    return ResponseEntity
        .status(HttpStatus.OK)
        .header(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + metaData.getFileName() + "\"")
        .header(HttpHeaders.CONTENT_TYPE, metaData.getContentType())
        .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(metaData.getSize()))
        .body(resource);
  }

  private Path resolvePath(UUID key) {
    return root.resolve(key.toString());
  }
}
