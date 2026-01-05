package com.codeit.closet.module.like.service.impl;

import com.codeit.closet.module.like.repository.LikeRepository;
import com.codeit.closet.module.like.service.LikeService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BasicLikeService implements LikeService {

  private final LikeRepository likeRepository;

  @Override
  public void createLike(UUID feedId) {

  }

  @Override
  public void deleteLike(UUID feedId) {

  }
}
