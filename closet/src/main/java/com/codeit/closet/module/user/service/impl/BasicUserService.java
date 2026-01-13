package com.codeit.closet.module.user.service.impl;

import com.codeit.closet.common.exception.ErrorCode;
import com.codeit.closet.common.exception.user.DuplicateUserException;
import com.codeit.closet.common.exception.user.UserNotFoundException;
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
import com.codeit.closet.module.weather.entity.WeatherRegion;
import com.codeit.closet.module.weather.service.WeatherService;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
@RequiredArgsConstructor
public class BasicUserService implements UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final BinaryContentService binaryContentService;
  private final WeatherService weatherService;

  private final UserMapper userMapper;

  @Override
  @CacheEvict(value = "users", allEntries = true)
  @Transactional
  public UserDTO createUser(UserCreateRequest request) {
    if (userRepository.existsByEmail(request.email())) {
      log.warn("이미 같은 아이디가 존재합니다. email = {}", request.email());
      throw DuplicateUserException.withEmail(request.email());
    }

    User user = User.builder()
        .name(request.name())
        .email(request.email())
        .password(passwordEncoder.encode(request.password()))
        .build();

    User save = userRepository.save(user);

    return userMapper.toUserDTO(save);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @Cacheable(value = "users", key = "{#cursor, #idAfter, #limit, #sortBy, #sortDirection, #emailLike, #roleEqual, #locked}")
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

    return userRepository.findUsersByCursor(cursor, idAfter, limit, sortBy, sortDirection,
        emailLike, roleEqual, locked);
  }

  @CacheEvict(value = "users", allEntries = true)
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
  @Cacheable(value = "userProfile", key = "#userId")
  @PreAuthorize("principal.userDTO.id == #userId")
  @Transactional(readOnly = true)
  public ProfileDTO findUserProfile(UUID userId) {
    User user = userRepository.findById(userId).orElseThrow(
        UserNotFoundException::new);

    return userMapper.toProfileDTO(user);
  }

  @Override
  @CacheEvict(value = "userProfile", key = "#userId")
  @PreAuthorize("principal.userDTO.id == #userId")
  @Transactional
  public ProfileDTO updateUserProfile(UUID userId, ProfileUpdateRequest request,
      MultipartFile multipartFile) {
    User user = userRepository.findById(userId).orElseThrow(
        () -> new NoSuchElementException("존재하지 않는 회원입니다."));

    BinaryContent binaryContent = null;
    if (multipartFile != null) {
      binaryContent = binaryContentService.createBinaryContent(multipartFile);
    }
    WeatherRegion weatherRegion = null;

    if (request.location() != null) {
      weatherRegion = weatherService.findWeatherRegion(request.location().longitude(),
          request.location().latitude());
    }

    user.updateProfile(request.name(), request.birthDate(),
        request.temperatureSensitivity(), request.gender(), weatherRegion, binaryContent);

    return userMapper.toProfileDTO(user);
  }

  @Override
  @PreAuthorize("principal.userDTO.id == #userId")
  @Transactional
  public void updateUserPassword(UUID userId, ChangePasswordRequest request) {
    User user = userRepository.findById(userId).orElseThrow(
        () -> new UserNotFoundException(ErrorCode.USER_NOT_FOUND,
            UserNotFoundException.withMessage("올바른 회원을 적어주세요")));

    String encodedNewPassword = passwordEncoder.encode(request.password());

    user.changePassword(encodedNewPassword);
  }


  @PreAuthorize("hasRole('ADMIN')")
  @CacheEvict(value = "users", allEntries = true)
  @Override
  @Transactional
  public UserDTO updateUserLock(UUID userId, UserLockUpdateRequest request) {
    User user = userRepository.findById(userId).orElseThrow(
        UserNotFoundException::new);

    user.updateLocked(request.locked());

    return userMapper.toUserDTO(user);
  }
}
