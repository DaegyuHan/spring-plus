package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public interface TodoRepository extends JpaRepository<Todo, Long>, TodoQueryRepository {

    @Query("SELECT DISTINCT t FROM Todo t JOIN FETCH t.managers JOIN t.comments " +
            "WHERE (:weather IS NULL OR t.weather = :weather) " +
            "AND (:start IS NULL OR t.modifiedAt >= :start) " +
            "AND (:end IS NULL OR t.modifiedAt <= :end)")
    Page<Todo> findTodosWithConditions(
            @Param("weather") String weather,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );
}
