package com.codeit.closet.module.user.service;

import com.codeit.closet.module.user.dto.profile.ProfileDTO;
import com.codeit.closet.module.user.dto.profile.ProfileUpdateRequest;
import com.codeit.closet.module.user.dto.user.*;

import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    UserDTO createUser(UserCreateRequest request);

    UserDTOCursorResponse findUsers(String cursor,
                                    UUID idAfter,
                                    Integer limit,
                                    String sortBy,
                                    String sortDirection,
                                    String emailLike,
                                    String roleEqual,
                                    Boolean locked);

    UserDTO updateUserRole(UUID userId, UserRoleUpdateRequest request);

    ProfileDTO findUserProfile(UUID userId);

    ProfileDTO updateUserProfile(UUID userId, ProfileUpdateRequest request, MultipartFile multipartFile);

    void updateUserPassword(UUID userId, ChangePasswordRequest request);

    UserDTO updateUserLock(UUID userId, UserLockUpdateRequest request);

}
