package com.codeit.closet.module.user.repository;

import com.codeit.closet.module.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> , UserQueryRepository{
    boolean existsByEmail(String email);

    boolean existsByName(String name);

    Optional<User> findByEmail(String email);
}
