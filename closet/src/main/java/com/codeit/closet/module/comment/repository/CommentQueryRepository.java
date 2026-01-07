package com.codeit.closet.module.comment.repository;

import com.codeit.closet.module.comment.dto.CommentDTOCursorResponse;
import java.util.UUID;

public interface CommentQueryRepository {

  CommentDTOCursorResponse findCommentsByCursor(UUID feedId, String cursor, UUID idAfter, Integer limit);
}
