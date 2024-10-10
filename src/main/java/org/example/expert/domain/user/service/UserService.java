package org.example.expert.domain.user.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserChangeImageResponse;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    @Value("${s3.bucket}")
    private String bucket;

    // S3
    private final AmazonS3Client s3Client;

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

    // 유저 프로필 사진 등록
    @Transactional
    public UserChangeImageResponse changeProfileImage(long userId, MultipartFile profileImage) throws IOException {
        // 유저 확인
        User user = userRepository.findById(userId).orElseThrow(()-> new IllegalArgumentException("Not Found User"));

        // 업로드한 파일의 S3 URL 주소
        String profileImageURL = uploadImageToS3(profileImage, bucket);

        // 유저 수정
        user.update(profileImageURL);

        // DB 저장
        userRepository.save(user);

        // Dto 반환
        return new UserChangeImageResponse(user);
    }

    // 유저 프로필 사진 삭제
    @Transactional
    public String deleteProfileImage(long userId) {
        // 유저 확인
        User user = userRepository.findById(userId).orElseThrow(()-> new IllegalArgumentException("Not Found User"));

        // 기존 등록된 URL 가지고 이미지 원본 이름 가져오기
        String profileImageFileName = extractFileNameFromUrl(user.getProfileImageURL());

        // 가져온 이미지 원본 이름으로 S3 이미지 삭제
        s3Client.deleteObject(bucket, profileImageFileName);

        // 유저 수정
        user.deleteProfileImage();

        // DB 저장
        userRepository.save(user);

        return "이미지 삭제가 완료되었습니다.";
    }

    // 닉네임으로 유저 조회 (정확히 일치)
    @Transactional
    public List<UserResponse> getUsersWithNickname(String nickname) {
        // nickname 변수로 user List 생성
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

    // 패스워드 조건 확인 메서드
    private static void validateNewPassword(UserChangePasswordRequest userChangePasswordRequest) {
        if (userChangePasswordRequest.getNewPassword().length() < 8 ||
                !userChangePasswordRequest.getNewPassword().matches(".*\\d.*") ||
                !userChangePasswordRequest.getNewPassword().matches(".*[A-Z].*")) {
            throw new InvalidRequestException("새 비밀번호는 8자 이상이어야 하고, 숫자와 대문자를 포함해야 합니다.");
        }
    }

    // 이미지 파일 이름 변경
    private String changeFileName(String originalFileName) {
        // 이미지 등록 날짜를 붙여서 리턴
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return LocalDateTime.now().format(formatter) + "_" + originalFileName;
    }

    // 이미지를 등록하고 URL 추출
    private String uploadImageToS3(MultipartFile image, String bucket) throws IOException {
        // 이미지 이름 변경
        String originalFileName = image.getOriginalFilename();
        String fileName = changeFileName(originalFileName);

        // S3에 파일을 보낼 때 파일의 종류와 크기를 알려주기
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(image.getContentType());
        metadata.setContentLength(image.getSize());
        metadata.setContentDisposition("inline");

        // S3에 파일 업로드
        s3Client.putObject(bucket, fileName, image.getInputStream(), metadata);

        return s3Client.getUrl(bucket, fileName).toString();
    }

    // 등록된 메뉴 기존 URL 원본 파일이름으로 디코딩
    private String extractFileNameFromUrl(String url) {
        try {
            // URL 마지막 슬래시의 위치를 찾아서 인코딩된 파일 이름 가져오기
            String encodedFileName = url.substring(url.lastIndexOf("/") + 1);

            // 인코딩된 파일 이름을 디코딩 해서 진짜 원본 파일 이름 가져오기
            return URLDecoder.decode(encodedFileName, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("원본 파일 이름 변경 에러", e);
        }
    }

    // 랜덤 닉네임 생성 메서드
    public static String generateRandomNickname() {
        return UUID.randomUUID().toString().substring(0, 8); // 8자리만 사용
    }


}
