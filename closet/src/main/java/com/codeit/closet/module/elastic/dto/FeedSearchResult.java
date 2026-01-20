package com.codeit.closet.module.elastic.dto;

import java.util.List;
import java.util.UUID;

public record FeedSearchResult(
    List<UUID> feedIds,
    String nextCursor,
    UUID nextIdAfter,
    boolean hasNext,
    long totalCount
) {}