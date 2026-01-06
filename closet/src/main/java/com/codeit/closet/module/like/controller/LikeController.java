package com.codeit.closet.module.like.controller;

import com.codeit.closet.common.security.ClosetUserDetails;
import com.codeit.closet.module.like.service.LikeService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feeds/{feedId}/like")
@RequiredArgsConstructor
public class LikeController {

  private final LikeService likeService;

  @PostMapping
  public ResponseEntity<Void> createLike(
      @PathVariable("feedId") UUID id,
      @AuthenticationPrincipal ClosetUserDetails userDetails) {
    UUID userId = userDetails.getUserDTO().id();
    likeService.createLike(id, userId);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping
  public ResponseEntity<Void> deleteLike(
      @PathVariable("feedId") UUID id,
      @AuthenticationPrincipal ClosetUserDetails userDetails) {
    UUID userId = userDetails.getUserDTO().id();
    likeService.deleteLike(id, userId);
    return ResponseEntity.ok().build();
  }
}
