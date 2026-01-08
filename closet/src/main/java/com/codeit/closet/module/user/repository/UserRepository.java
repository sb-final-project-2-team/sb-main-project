package com.codeit.closet.module.user.repository;

import com.codeit.closet.module.user.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface UserRepository extends JpaRepository<User, UUID>, UserQueryRepository {

  boolean existsByEmail(String email);

  boolean existsByName(String name);

  Optional<User> findByEmail(String email);

  @Transactional
  @Modifying(clearAutomatically = true)
  @Query("UPDATE User u SET u.tempPassword = NULL, u.tempPasswordExpiredAt = NULL WHERE u.id = :userId")
  void clearTempPassword(UUID userId);
}
