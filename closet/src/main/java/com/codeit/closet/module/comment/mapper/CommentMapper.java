package com.codeit.closet.module.comment.mapper;

import com.codeit.closet.module.comment.dto.CommentDTO;
import com.codeit.closet.module.comment.entity.Comment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CommentMapper {

  // 차후에 엔티티끼리 연동해야함.
  CommentDTO toDTO(Comment comment);
}
