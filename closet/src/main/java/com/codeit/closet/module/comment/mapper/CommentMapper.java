package com.codeit.closet.module.comment.mapper;

import com.codeit.closet.module.comment.dto.CommentDTO;
import com.codeit.closet.module.comment.entity.Comment;
import com.codeit.closet.module.user.mapper.UserMapper;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {
    UserMapper.class
})
public interface CommentMapper {

  // 차후에 엔티티끼리 연동해야함.
  @Mapping(target = "feedId", source = "comment.feed.id")
  @Mapping(target = "author", source = "comment.user")
  CommentDTO toDTO(Comment comment);

  List<CommentDTO> toDTOs(List<Comment> comments);
}
