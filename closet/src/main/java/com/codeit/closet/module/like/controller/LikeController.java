package com.codeit.closet.module.like.controller;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feeds/{feedId}/like")
@RequiredArgsConstructor
public class LikeController {

  // 차후에 연동 예정

  @PostMapping
  public ResponseEntity<Void> createLike(
      @PathVariable("feedId") UUID id) {
    return null;
  }

  @DeleteMapping
  public ResponseEntity<Void> deleteLike(
      @PathVariable("feedId") UUID id) {
    return null;
  }
}
