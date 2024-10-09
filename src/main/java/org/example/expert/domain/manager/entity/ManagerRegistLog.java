package org.example.expert.domain.manager.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.expert.domain.common.entity.Timestamped;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "log")
@EntityListeners(AuditingEntityListener.class)
public class ManagerRegistLog extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;

    private String registStatus;

    public ManagerRegistLog(String todoTitle, String userEmail, String managerEmail, ManagerRegistLogEnum registStatus) {
        this.message = userEmail + " 유저가 " + managerEmail + " 유저를 <" + todoTitle + "> 게시물의 담당자로 등록 요청했습니다.";
        this.registStatus = registStatus.getMessage();
    }

    // 매니저 등록 요청 로그 저장 메서드
    static public ManagerRegistLog createManagerRegistLog(String todoTitle, String userEmail, String managerEmail, ManagerRegistLogEnum registStatus) {
        return new ManagerRegistLog(todoTitle, userEmail, managerEmail, registStatus);
    }
}
