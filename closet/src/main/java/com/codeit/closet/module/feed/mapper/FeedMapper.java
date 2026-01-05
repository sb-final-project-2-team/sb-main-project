package com.codeit.closet.module.feed.mapper;

import com.codeit.closet.module.feed.dto.FeedDTO;
import com.codeit.closet.module.feed.entity.Feed;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FeedMapper {

  FeedDTO toDTO(Feed feed);
}
