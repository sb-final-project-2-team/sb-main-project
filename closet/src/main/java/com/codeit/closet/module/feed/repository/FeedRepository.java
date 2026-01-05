package com.codeit.closet.module.feed.repository;

import com.codeit.closet.module.feed.entity.Feed;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedRepository extends JpaRepository<Feed, UUID> {

}
