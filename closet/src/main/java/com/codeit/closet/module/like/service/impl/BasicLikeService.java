package com.codeit.closet.module.like.service.impl;

import com.codeit.closet.module.feed.entity.Feed;
import com.codeit.closet.module.feed.repository.FeedRepository;
import com.codeit.closet.module.like.entity.Like;
import com.codeit.closet.module.like.repository.LikeRepository;
import com.codeit.closet.module.like.service.LikeService;
import com.codeit.closet.module.user.entity.User;
import com.codeit.closet.module.user.repository.UserRepository;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BasicLikeService implements LikeService {

  private final FeedRepository feedRepository;
  private final UserRepository userRepository;
  private final LikeRepository likeRepository;

  @Override
  @Transactional
  public void createLike(UUID feedId, UUID userId) {
    Feed feed = feedRepository.findById(feedId).orElseThrow(
        () -> new NoSuchElementException("존재하지 않는 피드입니다."));
    User user = userRepository.findById(userId).orElseThrow(
        () -> new NoSuchElementException("존재하지 않는 유저입니다."));

    Like like = Like.builder()
        .user(user)
        .feed(feed)
        .build();

    feed.increaseLikeCount();

    likeRepository.save(like);
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
