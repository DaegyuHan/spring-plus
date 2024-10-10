package org.example.expert.domain.todo.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoProjectionDto;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoQueryRepositoryImpl;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoService {

    private final TodoRepository todoRepository;
    private final WeatherClient weatherClient;
    private final TodoQueryRepositoryImpl todoQueryRepositoryImpl;

    @Transactional
    public TodoSaveResponse saveTodo(AuthUser authUser, TodoSaveRequest todoSaveRequest) {
        User user = User.fromAuthUser(authUser);

        String weather = weatherClient.getTodayWeather();

        Todo newTodo = new Todo(
                todoSaveRequest.getTitle(),
                todoSaveRequest.getContents(),
                weather,
                user
        );
        Todo savedTodo = todoRepository.save(newTodo);

        return new TodoSaveResponse(
                savedTodo.getId(),
                savedTodo.getTitle(),
                savedTodo.getContents(),
                weather,
                new UserResponse(user.getId(), user.getEmail(), user.getNickname())
        );
    }

    // 할일 다건 조회
    public Page<TodoResponse> getTodos(int page, int size, String weather, Optional<LocalDate> start, Optional<LocalDate> end) {
        Pageable pageable = PageRequest.of(page - 1, size);

        // LocalDate 타입으로 받아온 start 를 LocalDateTime 으로 변환 및 미입력시 null 처리
        LocalDateTime startDateTime = start.
                map(LocalDate::atStartOfDay).
                orElse(null);

        // LocalDate 타입으로 받아온 end 를 LocalDateTime 으로 변환 및 미입력시 null 처리
        LocalDateTime endDateTime = end
                .map(e -> e.atTime(LocalTime.MAX))  // 해당 날짜의 23:59:59 까지로 설정
                .orElse(null);

        Page<Todo> todos = todoRepository.findTodosWithConditions(weather, startDateTime, endDateTime, pageable);

        return todos.map(todo -> new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getContents(),
                todo.getWeather(),
                new UserResponse(todo.getUser().getId(), todo.getUser().getEmail(), todo.getUser().getNickname()),
                todo.getCreatedAt(),
                todo.getModifiedAt()
        ));
    }


    public TodoResponse getTodo(long todoId) {
        Todo todo = todoRepository.findByIdByDsl(todoId);

        User user = todo.getUser();

        return new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getContents(),
                todo.getWeather(),
                new UserResponse(user.getId(), user.getEmail(), user.getNickname()),
                todo.getCreatedAt(),
                todo.getModifiedAt()
        );
    }

    public Page<TodoProjectionDto> getTodosProjection(int page, int size, String title, String nickname, LocalDate start, LocalDate end) {
        Pageable pageable = PageRequest.of(page - 1, size);

        return todoRepository.findByIdFromProjection(title, nickname, start, end, pageable);
    }
}
