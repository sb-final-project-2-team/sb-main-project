package com.codeit.closet.module.like.repository;

import com.codeit.closet.module.feed.entity.Feed;
import com.codeit.closet.module.like.entity.Like;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like, UUID>{

  boolean existsLikeByFeed(Feed feed);

  void deleteByFeed(Feed feed);
}
