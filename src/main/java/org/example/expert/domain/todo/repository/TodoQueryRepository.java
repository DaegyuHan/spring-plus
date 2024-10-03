package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.entity.Todo;

import java.util.List;

public interface TodoQueryRepository {
    Todo findByIdByDsl(long todoId);
}
