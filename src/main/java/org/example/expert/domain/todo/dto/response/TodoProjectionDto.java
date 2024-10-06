package org.example.expert.domain.todo.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

@Getter
public class TodoProjectionDto {

    private final String title;
    private final int mamnagerCount;
    private final int commentCount;

    @QueryProjection
    public TodoProjectionDto(String title, int mamnagerCount, int commentCount) {
        this.title = title;
        this.mamnagerCount = mamnagerCount;
        this.commentCount = commentCount;
    }

}
