package com.codeit.closet.module.comment.controller;

import com.codeit.closet.module.comment.dto.CommentCreateRequest;
import com.codeit.closet.module.comment.dto.CommentDTO;
import com.codeit.closet.module.feed.dto.FeedDTOCursorResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feeds/{feedId}/comments")
@RequiredArgsConstructor
public class CommentController {

  // 차후에 연동 예정

  @PostMapping
  public ResponseEntity<CommentDTO> createComment(
      @PathVariable("feedId") UUID id,
      @RequestBody CommentCreateRequest request) {
    return null;
  }

  @GetMapping
  public ResponseEntity<FeedDTOCursorResponse> getComments(@PathVariable("feedId") UUID id,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) UUID idAfter,
      @RequestParam Integer limit) {
    return null;
  }
}
