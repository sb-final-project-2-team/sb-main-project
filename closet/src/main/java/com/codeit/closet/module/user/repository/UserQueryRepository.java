package com.codeit.closet.module.user.repository;

import com.codeit.closet.module.user.dto.user.UserDTOCursorResponse;
import java.util.UUID;

public interface UserQueryRepository{

  UserDTOCursorResponse findUsersByCursor(
      String cursor,
      UUID idAfter,
      Integer limit,
      String sortBy,
      String sortDirection,
      String emailLike,
      String roleEqual,
      Boolean locked
  );
}
