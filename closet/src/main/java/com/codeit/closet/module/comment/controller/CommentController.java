package com.codeit.closet.module.comment.controller;

import com.codeit.closet.module.comment.dto.CommentCreateRequest;
import com.codeit.closet.module.comment.dto.CommentDTO;
import com.codeit.closet.module.comment.dto.CommentDTOCursorResponse;
import com.codeit.closet.module.comment.service.CommentService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

  private final CommentService commentService;

  @PostMapping
  public ResponseEntity<CommentDTO> createComment(
      @PathVariable("feedId") UUID id,
      @RequestBody CommentCreateRequest request) {

    CommentDTO comment = commentService.createComment(id, request);

    return ResponseEntity.status(HttpStatus.CREATED).body(comment);
  }

  @GetMapping
  public ResponseEntity<CommentDTOCursorResponse> getComments(@PathVariable("feedId") UUID id,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) UUID idAfter,
      @RequestParam Integer limit) {

    CommentDTOCursorResponse comments = commentService.getComments(id, cursor, idAfter, limit);

    return ResponseEntity.status(HttpStatus.OK).body(comments);
  }
}
