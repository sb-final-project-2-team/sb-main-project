package com.codeit.closet.module.like.repository;

import com.codeit.closet.module.feed.entity.Feed;
import com.codeit.closet.module.like.entity.Like;
import com.codeit.closet.module.user.entity.User;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like, UUID>{

  boolean existsByFeedAndUser(Feed feed, User user);

  void deleteByFeedAndUser(Feed feed, User user);
}
