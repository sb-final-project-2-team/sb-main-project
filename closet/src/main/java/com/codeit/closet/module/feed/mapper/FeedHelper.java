package com.codeit.closet.module.feed.mapper;

import org.mapstruct.Named;
import org.springframework.stereotype.Component;

@Component
public class FeedHelper {

  @Named("getLikedByMe")
  public boolean getLikedByMe(Boolean likedByMe) {
    return Boolean.TRUE.equals(likedByMe);
  }
}
