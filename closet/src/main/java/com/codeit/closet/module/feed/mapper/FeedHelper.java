package com.codeit.closet.module.feed.mapper;

import com.codeit.closet.module.feed.entity.Feed;
import com.codeit.closet.module.like.repository.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeedHelper {

  private final LikeRepository likeRepository;

  @Named("getLikedByMe")
  public boolean getLikedByMe(Feed feed) {
    return likeRepository.existsLikeByFeed(feed);
  }
}
