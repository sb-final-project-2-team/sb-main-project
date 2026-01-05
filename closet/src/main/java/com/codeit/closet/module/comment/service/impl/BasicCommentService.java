package com.codeit.closet.module.comment.service.impl;

import com.codeit.closet.module.comment.dto.CommentCreateRequest;
import com.codeit.closet.module.comment.dto.CommentDTO;
import com.codeit.closet.module.comment.dto.CommentDTOCursorResponse;
import com.codeit.closet.module.comment.mapper.CommentMapper;
import com.codeit.closet.module.comment.repository.CommentRepository;
import com.codeit.closet.module.comment.service.CommentService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BasicCommentService implements CommentService {

  // 차후에 연동할 예정
  private final CommentRepository commentRepository;
  private final CommentMapper commentMapper;

  @Override
  public CommentDTO createComment(UUID feedId, CommentCreateRequest request) {
    return null;
  }

  @Override
  public CommentDTOCursorResponse getComments(UUID feedId, String cursor, UUID idAfter,
      Integer limit) {
    return null;
  }
}
