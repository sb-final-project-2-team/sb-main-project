package com.codeit.closet.module.user.service.impl;

import com.codeit.closet.module.user.dto.profile.ProfileDTO;
import com.codeit.closet.module.user.dto.profile.ProfileUpdateRequest;
import com.codeit.closet.module.user.dto.user.*;
import com.codeit.closet.module.user.repository.UserRepository;
import com.codeit.closet.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicUserService implements UserService {
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDTO createUser(UserCreateRequest request) {
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTOCursorResponse findUsers(String cursor,
                                           UUID idAfter,
                                           Integer limit,
                                           String sortBy,
                                           String sortDirection,
                                           String emailLike,
                                           String roleEqual,
                                           Boolean locked) {
        return null;
    }

    @Override
    @Transactional
    public UserDTO updateUserRole(UUID userId, UserRoleUpdateRequest request) {
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileDTO findUserProfile(UUID userId) {
        return null;
    }

    @Override
    @Transactional
    public ProfileDTO updateUserProfile(UUID userId, ProfileUpdateRequest request) {
        return null;
    }

    @Override
    @Transactional
    public void updateUserPassword(UUID userId, ChangePasswordRequest request) {

    }

    @Override
    public UserDTO updateUserLock(UUID userId, UserLockUpdateRequest request) {
        return null;
    }
}
