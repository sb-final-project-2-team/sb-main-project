package com.codeit.closet.module.comment.repository;

import com.codeit.closet.module.comment.entity.Comment;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, UUID> , CommentQueryRepository{

}
