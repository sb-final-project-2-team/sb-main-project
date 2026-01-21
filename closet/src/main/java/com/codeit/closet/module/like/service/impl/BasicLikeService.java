package com.codeit.closet.module.like.service.impl;

import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codeit.closet.module.feed.entity.Feed;
import com.codeit.closet.module.feed.repository.FeedRepository;
import com.codeit.closet.module.like.entity.Like;
import com.codeit.closet.module.like.repository.LikeRepository;
import com.codeit.closet.module.like.service.LikeService;
import com.codeit.closet.module.notification.event.NotifyUserEvent;
import com.codeit.closet.module.notification.template.NotificationTemplate;
import com.codeit.closet.module.user.entity.User;
import com.codeit.closet.module.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BasicLikeService implements LikeService {

  private final FeedRepository feedRepository;
  private final UserRepository userRepository;
  private final LikeRepository likeRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  @Transactional
  public void createLike(UUID feedId, UUID userId) {
    Feed feed = feedRepository.findById(feedId).orElseThrow(
        () -> new NoSuchElementException("존재하지 않는 피드입니다."));
    User user = userRepository.findById(userId).orElseThrow(
        () -> new NoSuchElementException("존재하지 않는 유저입니다."));

    UUID feedOwnerId = feed.getUser().getId();

    // 중복 좋아요 방지용
    if (likeRepository.existsByFeedAndUser(feed, user)) {
      return;
    }

    Like like = Like.builder()
        .user(user)
        .feed(feed)
        .build();

    feed.increaseLikeCount();
    likeRepository.save(like);

    if (feedOwnerId.equals(userId)) {
      return;
    }

    eventPublisher.publishEvent(
        new NotifyUserEvent(
            feedOwnerId,
            NotificationTemplate.LIKE,
            null,
            new Object[]{user.getName()},
            new Object[]{user.getName()}
        )
    );
  }

  @Override
  @Transactional
  public void deleteLike(UUID feedId, UUID userId) {

    Feed feed = feedRepository.findById(feedId).orElseThrow(
        () -> new NoSuchElementException("존재하지 않는 피드입니다."));

    User user = userRepository.findById(userId).orElseThrow(
        () -> new NoSuchElementException("존재하지 않는 유저입니다."));
    feed.decreaseLikeCount();

    likeRepository.deleteByFeedAndUser(feed, user);
  }
}
