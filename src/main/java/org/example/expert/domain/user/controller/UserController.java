package org.example.expert.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.common.annotation.Auth;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserChangeImageResponse;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getUsersWithNickname(@RequestParam String nickname) {
        return ResponseEntity.ok(userService.getUsersWithNickname(nickname));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable long userId) {
        return ResponseEntity.ok(userService.getUser(userId));
    }

    @PutMapping("/users")
    public void changePassword(@AuthenticationPrincipal AuthUser authUser, @RequestBody UserChangePasswordRequest userChangePasswordRequest) {
        long userId = Long.parseLong(authUser.getUserId());
        userService.changePassword(userId, userChangePasswordRequest);
    }

    // 유저 프로필 사진 등록
    @PutMapping("/users/profile")
    public ResponseEntity<UserChangeImageResponse> changeProfileImage(@AuthenticationPrincipal AuthUser authUser, @RequestPart(name = "image", required = false) MultipartFile profileImage) throws IOException {
        long userId = Long.parseLong(authUser.getUserId());
        return ResponseEntity.ok(userService.changeProfileImage(userId, profileImage));
    }

    // 유저 프로필 사진 삭제
    @DeleteMapping("/users/profile")
    public ResponseEntity<String> deleteProfileImage(@AuthenticationPrincipal AuthUser authUser) {
        long userId = Long.parseLong(authUser.getUserId());
        return ResponseEntity.ok(userService.deleteProfileImage(userId));
    }

    // 유저 백만 건 저장
    @PostMapping("/pushUsers")
    public ResponseEntity<String> pushUsers() {
        return ResponseEntity.ok(userService.pushUsers());
    }
}
