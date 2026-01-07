package com.codeit.closet.module.like.service;

import java.util.UUID;

public interface LikeService {

  void createLike(UUID feedId, UUID userId);

  void deleteLike(UUID feedId, UUID userId);
}
