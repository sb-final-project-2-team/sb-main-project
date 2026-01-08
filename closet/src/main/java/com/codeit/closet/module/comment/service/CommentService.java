package com.codeit.closet.module.comment.service;

import com.codeit.closet.module.comment.dto.CommentCreateRequest;
import com.codeit.closet.module.comment.dto.CommentDTO;
import com.codeit.closet.module.comment.dto.CommentDTOCursorResponse;
import java.util.UUID;

public interface CommentService {

  CommentDTO createComment(UUID feedId, CommentCreateRequest request);

  CommentDTOCursorResponse getComments(UUID feedId, String cursor, UUID idAfter, Integer limit);
}
