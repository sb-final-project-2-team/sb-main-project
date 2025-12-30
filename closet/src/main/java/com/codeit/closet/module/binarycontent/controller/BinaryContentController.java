package com.codeit.closet.module.binarycontent.controller;

import com.codeit.closet.module.binarycontent.entity.BinaryContent;
import com.codeit.closet.module.binarycontent.service.BinaryContentService;
import com.codeit.closet.module.binarycontent.storage.BinaryContentStorage;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/binaryContents")
@RequiredArgsConstructor
public class BinaryContentController {

  private final BinaryContentService binaryContentService;
  private final BinaryContentStorage binaryContentStorage;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<BinaryContent> saveBinaryContent(
      @RequestPart("multipartFile") MultipartFile multipartFile) {
    BinaryContent binaryContent = binaryContentService.createBinaryContent(multipartFile);
    return ResponseEntity.status(HttpStatus.CREATED).body(binaryContent);
  }

  @GetMapping("/findUrl/{id}")
  public ResponseEntity<String> getFileUrl(@PathVariable UUID id) {
    return ResponseEntity.ok(binaryContentService.findFileUrlByBinaryContentId(id));
  }

  @GetMapping("/{binaryContentId}/download")
  public ResponseEntity<Resource> downloadBinaryContent(
      @PathVariable(name = "binaryContentId") UUID id) {
    BinaryContent binaryContent = binaryContentService.findByBinaryContentId(id);
    return binaryContentStorage.download(binaryContent);
  }

  @DeleteMapping("/{binaryContentId}")
  public ResponseEntity<Void> deleteBinaryContent(@PathVariable(name = "binaryContentId") UUID id) {
    binaryContentService.deleteBinaryContent(id);
    return ResponseEntity.ok().build();
  }
}
