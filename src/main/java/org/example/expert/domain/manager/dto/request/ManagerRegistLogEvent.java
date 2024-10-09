package org.example.expert.domain.manager.dto.request;

import lombok.Getter;
import org.example.expert.domain.manager.entity.ManagerRegistLogEnum;

@Getter
public class ManagerRegistLogEvent {

    private final String todoTitle;
    private final String userEmail;
    private final String managerEmail;
    private final ManagerRegistLogEnum managerRegistLogEnum;

    public ManagerRegistLogEvent(String todoTitle, String userEmail, String managerEmail, ManagerRegistLogEnum managerRegistLogEnum) {
        this.todoTitle = todoTitle != null? todoTitle : "Unknown Title";
        this.userEmail = userEmail != null? userEmail : "Unknown Email";
        this.managerEmail = managerEmail != null? managerEmail : "Unknown Email";
        this.managerRegistLogEnum = managerRegistLogEnum;
    }
}
