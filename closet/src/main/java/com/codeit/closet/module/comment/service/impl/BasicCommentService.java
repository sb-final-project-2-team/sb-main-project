package com.codeit.closet.module.comment.service.impl;

import com.codeit.closet.module.comment.dto.CommentCreateRequest;
import com.codeit.closet.module.comment.dto.CommentDTO;
import com.codeit.closet.module.comment.dto.CommentDTOCursorResponse;
import com.codeit.closet.module.comment.entity.Comment;
import com.codeit.closet.module.comment.mapper.CommentMapper;
import com.codeit.closet.module.comment.repository.CommentRepository;
import com.codeit.closet.module.comment.service.CommentService;
import com.codeit.closet.module.feed.entity.Feed;
import com.codeit.closet.module.feed.repository.FeedRepository;
import com.codeit.closet.module.notification.event.NotifyUserEvent;
import com.codeit.closet.module.notification.service.NotificationService;
import com.codeit.closet.module.notification.template.NotificationTemplate;
import com.codeit.closet.module.user.entity.User;
import com.codeit.closet.module.user.repository.UserRepository;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BasicCommentService implements CommentService {

  private final UserRepository userRepository;
  private final FeedRepository feedRepository;
  private final CommentRepository commentRepository;
  private final CommentMapper commentMapper;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  @Transactional
  public CommentDTO createComment(UUID feedId, CommentCreateRequest request) {

    Feed feed = feedRepository.findById(feedId).orElseThrow(
        () -> new NoSuchElementException("존재하지 않는 피드 입니다."));

    User user = userRepository.findById(request.authorId()).orElseThrow(
        () -> new NoSuchElementException("존재하지않는 회원입니다."));

    UUID feedOwnerId = feed.getUser().getId();

    Comment comment = Comment.builder()
        .user(user)
        .feed(feed)
        .content(request.content())
        .build();

    feed.increaseCommentCount();

    Comment save = commentRepository.save(comment);

    if (!feedOwnerId.equals(user.getId())) {
      eventPublisher.publishEvent(
          new NotifyUserEvent(
              feedOwnerId,
              NotificationTemplate.COMMENT,
              null,
              new Object[]{user.getName()},
              new Object[]{user.getName()}
          )
      );
    }


    return commentMapper.toDTO(save);
  }

  @Override
  @Transactional
  public CommentDTOCursorResponse getComments(UUID feedId, String cursor, UUID idAfter,
      Integer limit) {

    return commentRepository.findCommentsByCursor(feedId, cursor, idAfter, limit);
  }
}
