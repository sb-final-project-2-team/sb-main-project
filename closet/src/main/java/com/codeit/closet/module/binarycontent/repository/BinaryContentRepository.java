package com.codeit.closet.module.binarycontent.repository;

import java.util.UUID;
import com.codeit.closet.module.binarycontent.entity.BinaryContent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BinaryContentRepository extends JpaRepository<BinaryContent, UUID> {

}
