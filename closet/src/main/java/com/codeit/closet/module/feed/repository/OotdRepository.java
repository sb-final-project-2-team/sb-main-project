package com.codeit.closet.module.feed.repository;

import com.codeit.closet.module.feed.entity.Ootd;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OotdRepository extends JpaRepository<Ootd, UUID> {

}
