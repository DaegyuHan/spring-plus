package org.example.expert.domain.user.dto.response;

import lombok.Getter;
import org.example.expert.domain.user.entity.User;

@Getter
public class UserChangeImageResponse {

    private final Long id;
    private final String email;
    private final String nickname;
    private final String profileImage;

    public UserChangeImageResponse(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.profileImage = user.getProfileImageURL();
    }
}
