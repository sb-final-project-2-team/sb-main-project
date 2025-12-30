package com.codeit.closet.module.user.controller;

import com.codeit.closet.module.user.dto.profile.ProfileDTO;
import com.codeit.closet.module.user.dto.profile.ProfileUpdateRequest;
import com.codeit.closet.module.user.dto.user.*;
import com.codeit.closet.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody UserCreateRequest request) {
        return ResponseEntity.ok().build();
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
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{userId}/role")
    public ResponseEntity<UserDTO> updateUserRole(@PathVariable UUID userId,
                                                  @RequestBody UserRoleUpdateRequest request) {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}/profiles")
    public ResponseEntity<ProfileDTO> getUserProfile(@PathVariable UUID userId) {
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{userId}/profiles")
    public ResponseEntity<ProfileDTO> updateUserProfile(@PathVariable UUID userId,
                                                        @RequestBody ProfileUpdateRequest request) { // 이미지도 가능하게
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{userId}/password")
    public ResponseEntity<Void> updateUserPassword(@PathVariable UUID userId,
                                                   @RequestBody ChangePasswordRequest request) {
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{userId}/lock")
    public ResponseEntity<UserDTO> updateUserLock(@PathVariable UUID userId,
                                                  @RequestBody UserLockUpdateRequest request) {

        return ResponseEntity.ok().build();
    }
}
