package org.example.expert.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse getUser(long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new InvalidRequestException("User not found"));
        return new UserResponse(user.getId(), user.getEmail(), user.getNickname());
    }

    @Transactional
    public void changePassword(long userId, UserChangePasswordRequest userChangePasswordRequest) {
        validateNewPassword(userChangePasswordRequest);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidRequestException("User not found"));

        if (passwordEncoder.matches(userChangePasswordRequest.getNewPassword(), user.getPassword())) {
            throw new InvalidRequestException("새 비밀번호는 기존 비밀번호와 같을 수 없습니다.");
        }

        if (!passwordEncoder.matches(userChangePasswordRequest.getOldPassword(), user.getPassword())) {
            throw new InvalidRequestException("잘못된 비밀번호입니다.");
        }

        user.changePassword(passwordEncoder.encode(userChangePasswordRequest.getNewPassword()));
    }

    private static void validateNewPassword(UserChangePasswordRequest userChangePasswordRequest) {
        if (userChangePasswordRequest.getNewPassword().length() < 8 ||
                !userChangePasswordRequest.getNewPassword().matches(".*\\d.*") ||
                !userChangePasswordRequest.getNewPassword().matches(".*[A-Z].*")) {
            throw new InvalidRequestException("새 비밀번호는 8자 이상이어야 하고, 숫자와 대문자를 포함해야 합니다.");
        }
    }

    // 닉네임으로 유저 조회 (정확히 일치)
    public List<UserResponse> getUsersWithNickname(String nickname) {
        List<User> users = userRepository.findUsersByNickname(nickname);

        return users.stream()
                .map(user -> new UserResponse(user.getId(), user.getEmail(), user.getNickname()))
                .collect(Collectors.toList());
    }

    // 유저 백만 건 데이터베이스에 저장
    @Transactional
    public String pushUsers() {
        int batchSize = 1000;
        List<User> users = new ArrayList<>(batchSize);
        Set<String> existingNicknames = new HashSet<>();

        for (int i = 1; i<=1000000; i++) {
            String randomNickname;

            // 중복되지 않는 닉네임 생성
            do {
                randomNickname = generateRandomNickname();
            } while (existingNicknames.contains(randomNickname)); // 중복 체크
            existingNicknames.add(randomNickname); // 생성된 닉네임을 Set 에 추가

            User user = new User("email"+i+"@gmail.com", "password", randomNickname, UserRole.ROLE_USER);
            users.add(user);

            // batchSize 가 채워질 때 마다 users List 저장하고 List 초기화
        if (i % batchSize == 0) {
            userRepository.saveAll(users);
            users.clear();
        }
    }

    // 남아있는 유저 저장
    if (!users.isEmpty()) {
        userRepository.saveAll(users);
    }
        return "랜덤 유저 백만 개 저장 성공";
    }


    public static String generateRandomNickname() {
        return UUID.randomUUID().toString().substring(0, 8); // 8자리만 사용
    }
}
