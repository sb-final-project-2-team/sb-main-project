package com.codeit.closet.module.user.controller;

import com.codeit.closet.module.user.dto.profile.ProfileDTO;
import com.codeit.closet.module.user.dto.profile.ProfileUpdateRequest;
import com.codeit.closet.module.user.dto.user.ChangePasswordRequest;
import com.codeit.closet.module.user.dto.user.UserCreateRequest;
import com.codeit.closet.module.user.dto.user.UserDTO;
import com.codeit.closet.module.user.dto.user.UserDTOCursorResponse;
import com.codeit.closet.module.user.dto.user.UserLockUpdateRequest;
import com.codeit.closet.module.user.dto.user.UserRoleUpdateRequest;
import com.codeit.closet.module.user.service.UserService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @PostMapping
  public ResponseEntity<UserDTO> createUser(@RequestBody UserCreateRequest request) {
    UserDTO result = userService.createUser(request);
    return ResponseEntity.status(HttpStatus.OK).body(result);
  }

  @GetMapping
  public ResponseEntity<UserDTOCursorResponse> getUsers(
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) UUID idAfter,
      @RequestParam Integer limit,
      @RequestParam String sortBy,
      @RequestParam String sortDirection,
      @RequestParam(required = false) String emailLike,
      @RequestParam(required = false) String roleEqual,
      @RequestParam(required = false) Boolean locked
  ) {
    UserDTOCursorResponse results = userService.findUsers(cursor, idAfter, limit, sortBy,
        sortDirection, emailLike, roleEqual, locked);
    return ResponseEntity.status(HttpStatus.OK).body(results);
  }

  @PatchMapping("/{userId}/role")
  public ResponseEntity<UserDTO> updateUserRole(@PathVariable UUID userId,
      @RequestBody UserRoleUpdateRequest request) {
    UserDTO result = userService.updateUserRole(userId, request);
    return ResponseEntity.status(HttpStatus.OK).body(result);
  }

  @GetMapping("/{userId}/profiles")
  public ResponseEntity<ProfileDTO> getUserProfile(@PathVariable UUID userId) {
    ProfileDTO result = userService.findUserProfile(userId);
    return ResponseEntity.status(HttpStatus.OK).body(result);
  }

  @PatchMapping(value = "/{userId}/profiles", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ProfileDTO> updateUserProfile(@PathVariable UUID userId,
      @RequestPart ProfileUpdateRequest request,
      @RequestPart(value = "image", required = false) MultipartFile multipartFile){

    ProfileDTO result = userService.updateUserProfile(userId, request, multipartFile);
    return ResponseEntity.status(HttpStatus.OK).body(result);
  }

  @PatchMapping("/{userId}/password")
  public ResponseEntity<Void> updateUserPassword(@PathVariable UUID userId,
      @RequestBody ChangePasswordRequest request) {
    userService.updateUserPassword(userId, request);
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  @PatchMapping("/{userId}/lock")
  public ResponseEntity<UserDTO> updateUserLock(@PathVariable UUID userId,
      @RequestBody UserLockUpdateRequest request) {
    UserDTO result = userService.updateUserLock(userId, request);
    return ResponseEntity.status(HttpStatus.OK).body(result);
  }
}
