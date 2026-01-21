package com.codeit.closet.module.feed.repository;

import com.codeit.closet.module.feed.entity.Feed;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FeedRepository extends JpaRepository<Feed, UUID>, FeedQueryRepository {

  @Query("""
        select distinct f from Feed f
        join fetch f.user
        join fetch f.weather
        left join fetch f.ootds o
        left join fetch o.cloth
        where f.id in :ids
      """)
  List<Feed> findFeedsByIdIn(Collection<UUID> ids);
}
