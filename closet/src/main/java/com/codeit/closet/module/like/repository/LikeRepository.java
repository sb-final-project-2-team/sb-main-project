package com.codeit.closet.module.like.repository;

import com.codeit.closet.module.like.entity.Like;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like, UUID> {

}
