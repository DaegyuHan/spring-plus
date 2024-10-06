package org.example.expert.domain.todo.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.todo.dto.response.QTodoProjectionDto;
import org.example.expert.domain.todo.dto.response.TodoProjectionDto;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.example.expert.domain.todo.entity.QTodo.todo;
import static org.example.expert.domain.user.entity.QUser.user;

@Repository
@RequiredArgsConstructor
public class TodoQueryRepositoryImpl implements TodoQueryRepository{

    private final JPAQueryFactory queryFactory;

    @Override
    public Todo findByIdByDsl(long todoId) {
        return queryFactory
                .select(todo)
                .from(todo)
                .join(todo.user, user).fetchJoin()
                .where(
                        todoIdEq(todoId)
                ).fetchOne();
    }

    // Projection 검색
    @Override
    public Page<TodoProjectionDto> findByIdFromProjection(String title, String nickname, LocalDate start, LocalDate end, Pageable pageable) {

        BooleanBuilder builder = new BooleanBuilder();

        // 제목 검색
        if (title != null && !title.isEmpty()) {
            builder.and(titleContains(title));
        }

        // 닉네임 검색
        if (nickname != null && !nickname.isEmpty()) {
            builder.and(nicknameContains(nickname));
        }

        // 생성일 범위 검색
        if (start != null && end != null) {
            builder.and(createdDateBetween(start, end));
        } else if (start != null) {
            builder.and(createdDateAfter(start));
        } else if (end != null) {
            builder.and(createdDateBefore(end));
        }

        List<TodoProjectionDto> content = queryFactory
                .select(
                        new QTodoProjectionDto(
                                todo.title,             // 할일 제목
                                todo.managers.size(),   // 담당자 수
                                todo.comments.size()    // 댓글 수
                        )
                )
                .from(todo)
                .leftJoin(todo.managers)
                .leftJoin(todo.comments)
                .where(builder)
                .orderBy(todo.createdAt.desc())     // 생성일 최신순 조회
                .offset(pageable.getOffset())       // 페이지 넘버
                .limit(pageable.getPageSize())      // 페이지 크기 ( 페이지당 할일 표시 수 )
                .fetch();

        long total = Optional.ofNullable(queryFactory
                .select(todo.count())
                .from(todo)
                .where(builder)
                .fetchOne())
                .orElse(0L);

        return new PageImpl<>(content, pageable, total);
    }

    // todoId 로 조회 메서드
    private BooleanExpression todoIdEq(Long todoId) {
        return todoId != null ? todo.id.eq(todoId) : null;
    }

    // 제목 검색 포함 조회 메서드
    private BooleanExpression titleContains(String title) {
        return title != null ? todo.title.contains(title) : null ;
    }

    // 닉네임 검색 포함 조회 메서드
    private BooleanExpression nicknameContains(String nickname) {
        return nickname != null ? todo.managers.any().user.nickname.contains(nickname) : null ;
    }

    // 생성일 범위 검색 메서드
    private BooleanExpression createdDateBetween(LocalDate start, LocalDate end) {
        return todo.createdAt.between(start.atStartOfDay(), end.atTime(LocalTime.MAX));
    }

    // start 날짜 이상 범위 검색 메서드
    private BooleanExpression createdDateAfter(LocalDate start) {
        return todo.createdAt.goe(start.atStartOfDay());
    }

    // end 날짜 이하 범위 검색 메서드
    private BooleanExpression createdDateBefore(LocalDate end) {
        return todo.createdAt.loe(end.atTime(LocalTime.MAX));
    }
}
