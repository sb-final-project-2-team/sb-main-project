package com.codeit.closet.module.user.service.impl;

import com.codeit.closet.module.binarycontent.entity.BinaryContent;
import com.codeit.closet.module.binarycontent.service.BinaryContentService;
import com.codeit.closet.module.user.dto.profile.ProfileDTO;
import com.codeit.closet.module.user.dto.profile.ProfileUpdateRequest;
import com.codeit.closet.module.user.dto.user.ChangePasswordRequest;
import com.codeit.closet.module.user.dto.user.UserCreateRequest;
import com.codeit.closet.module.user.dto.user.UserDTO;
import com.codeit.closet.module.user.dto.user.UserDTOCursorResponse;
import com.codeit.closet.module.user.dto.user.UserLockUpdateRequest;
import com.codeit.closet.module.user.dto.user.UserRoleUpdateRequest;
import com.codeit.closet.module.user.entity.User;
import com.codeit.closet.module.user.mapper.UserMapper;
import com.codeit.closet.module.user.repository.UserRepository;
import com.codeit.closet.module.user.service.UserService;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class BasicUserService implements UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final BinaryContentService binaryContentService;

  @Override
  @Transactional
  public UserDTO createUser(UserCreateRequest request) {
    if (userRepository.existsByEmail(request.email())) {
      throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
    }

    User user = User.builder()
        .name(request.name())
        .email(request.email())
        .password(passwordEncoder.encode(request.password()))
        .build();

    User save = userRepository.save(user);

    return userMapper.toUserDTO(save);
  }

  @Override
  @Transactional(readOnly = true)
  public UserDTOCursorResponse findUsers(
      String cursor,
      UUID idAfter,
      Integer limit,
      String sortBy,
      String sortDirection,
      String emailLike,
      String roleEqual,
      Boolean locked) {

    return userRepository.findUsersByCursor(cursor, idAfter, limit, sortBy, sortDirection, emailLike, roleEqual, locked);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @Override
  @Transactional
  public UserDTO updateUserRole(UUID userId, UserRoleUpdateRequest request) {
    User user = userRepository.findById(userId).orElseThrow(
        () -> new NoSuchElementException("존재하지 않는 회원입니다."));

    user.updateRole(request.role());

    return userMapper.toUserDTO(user);
  }

  @Override
  @Transactional(readOnly = true)
  public ProfileDTO findUserProfile(UUID userId) {
    User user = userRepository.findById(userId).orElseThrow(
        () -> new NoSuchElementException("존재하지 않는 회원입니다."));

    return userMapper.toProfileDTO(user);
  }

  @Override
  @Transactional
  public ProfileDTO updateUserProfile(UUID userId, ProfileUpdateRequest request,
      MultipartFile multipartFile) {
    User user = userRepository.findById(userId).orElseThrow(
        () -> new NoSuchElementException("존재하지 않는 회원입니다."));

    BinaryContent binaryContent = null;
    if (multipartFile != null) {
      binaryContent = binaryContentService.createBinaryContent(multipartFile);
    }

    // 차후에 Location 처리 넣어야함.

    user.updateProfile(request.name(), request.birthDate(),
        request.temperatureSensitivity(), request.gender(), binaryContent);

    return userMapper.toProfileDTO(user);
  }

  // 이메일 인증을 통한 비밀번호 리셋용 (별도 검증 로직 추가 예정 )
  @Override
  @Transactional
  public void updateUserPassword(UUID userId, ChangePasswordRequest request) {
    User user = userRepository.findById(userId).orElseThrow(
        () -> new NoSuchElementException("존재하지 않는 회원입니다."));

    String encodedNewPassword = passwordEncoder.encode(request.password());

    user.changePassword(encodedNewPassword);
  }


  @PreAuthorize("hasRole('ADMIN')")
  @Override
  @Transactional
  public UserDTO updateUserLock(UUID userId, UserLockUpdateRequest request) {
    User user = userRepository.findById(userId).orElseThrow(
        () -> new NoSuchElementException("존재하지 않는 회원입니다."));

    user.updateLocked(request.locked());

    return userMapper.toUserDTO(user);
  }
}
