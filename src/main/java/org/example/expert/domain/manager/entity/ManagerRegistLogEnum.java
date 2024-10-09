package org.example.expert.domain.manager.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ManagerRegistLogEnum {

    REGIST_LOG_SUCCESS("등록 성공"),

    REGIST_LOG_FAIL_DUBLICATED_USER("등록 실패 - 본인을 담당자로 등록 요청"),

    REGIST_LOG_FAIL_NOT_FOUND_MANAGER("등록 실패 - 등록하려는 유저 조회 실패"),

    REGIST_LOG_FAIL__NOT_FOUND_USER("등록 실패 - 로그인 유저 조회 실패"),

    REGIST_LOG_FAIL__NOT_FOUND_TODO("등록 실패 - 할 일 조회 실패");

    private final String message;
}
