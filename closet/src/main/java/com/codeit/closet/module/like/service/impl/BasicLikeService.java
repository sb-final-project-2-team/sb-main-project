package com.codeit.closet.module.like.service.impl;

import com.codeit.closet.module.feed.entity.Feed;
import com.codeit.closet.module.feed.repository.FeedRepository;
import com.codeit.closet.module.like.entity.Like;
import com.codeit.closet.module.like.repository.LikeRepository;
import com.codeit.closet.module.like.service.LikeService;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BasicLikeService implements LikeService {

  private final FeedRepository feedRepository;
  private final LikeRepository likeRepository;

  @Override
  @Transactional
  public void createLike(UUID feedId) {
    Feed feed = feedRepository.findById(feedId).orElseThrow(
        () -> new NoSuchElementException("존재하지 않는 피드입니다."));

    Like like = Like.builder()
        .user(feed.getUser())
        .feed(feed)
        .build();

    feed.increaseLikeCount();

    likeRepository.save(like);
  }

  @Override
  @Transactional
  public void deleteLike(UUID feedId) {

    Feed feed = feedRepository.findById(feedId).orElseThrow(
        () -> new NoSuchElementException("존재하지 않는 피드입니다."));

    feed.decreaseLikeCount();

    likeRepository.deleteByFeed(feed);
  }
}
