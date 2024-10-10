package org.example.expert.domain.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.entity.Timestamped;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.security.core.GrantedAuthority;

import java.util.stream.Collectors;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "users", indexes = @Index(name = "idx_nickname", columnList = "nickname"))
public class User extends Timestamped {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String email;
    private String password;
    @Enumerated(EnumType.STRING)
    private UserRole userRole;
    // 유저 닉네임
    private String nickname;
    // 유저 프로필 사진
    private String profileImageURL = "기본 이미지";

    public User(String email, String password, String nickname, UserRole userRole) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.userRole = userRole;
    }

    private User(Long id, String email, String nickname, UserRole userRole) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.userRole = userRole;
    }

    public static User fromAuthUser(AuthUser authUser) {
        String roleName = authUser.getAuthorities().iterator().next().getAuthority();
        long userId = Long.parseLong(authUser.getUserId());
        return new User(userId, authUser.getEmail(),authUser.getNickname(), UserRole.of(roleName));
    }

    public void changePassword(String password) {
        this.password = password;
    }

    public void updateRole(UserRole userRole) {
        this.userRole = userRole;
    }

    // 프로필 사진 업로드
    public void update(String profileImageURL) {
        this.profileImageURL = profileImageURL;
    }

    // 프로필 사진 삭제 ( null 로 변경 -> 프론트에서 기본 이미지 같은 형태로 응용 가능 )
    public void deleteProfileImage() {
        this.profileImageURL = "기본 이미지";
    }
}
