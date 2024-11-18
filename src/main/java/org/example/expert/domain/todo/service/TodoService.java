package org.example.expert.domain.todo.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.Todo;
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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoService {

    private final TodoRepository todoRepository;
    private final WeatherClient weatherClient;

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

    public Page<TodoResponse> getTodos(String weather, LocalDate startDate, LocalDate endDate, int page, int size) {

        // 예외처리
        // 기간조건 중 시작일이나 종료일 둘 중 하나라도 입력된 경우 예외처리 로직 실행
        if(startDate != null || endDate != null) {
            validateGetTodo(startDate, endDate);
        }

        Pageable pageable = PageRequest.of(page - 1, size);

        Page<Todo> todos;

        // weather 조건과 기간 조건이 있는 경우
        if(weather != null && startDate != null && endDate != null) {
            LocalDateTime startDateTime = convertToStartDateTime(startDate);
            LocalDateTime endDateTime = convertToEndDateTime(endDate);
            todos = todoRepository.findByWeatherAndDate(weather, startDateTime, endDateTime, pageable);
        // weather 조건만 있는 경우
        } else if (weather != null) {
            todos = todoRepository.findByWeatherOrOrderByModifiedAt(weather, pageable);
        // 기간 조건만 있는 경우
        } else if (startDate != null && endDate != null) {
            LocalDateTime startDateTime = convertToStartDateTime(startDate);
            LocalDateTime endDateTime = convertToEndDateTime(endDate);
            todos = todoRepository.findByDateOrderByModifiedAt(startDateTime, endDateTime, pageable);
        // 아무 조건도 없는 경우 전체 조회
        } else {
            todos = todoRepository.findAllByOrderByModifiedAtDesc(pageable);
        }

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

    // 예외처리
    public void validateGetTodo( LocalDate startDate, LocalDate endDate) {
        // weather 입력값이 옳지 않은 경우 > weather 입력값을 전부 알 수가 없으므로 제외
        // startDate, endDate 가 LocalDate 형식이 아닌 경우 -> 컨트롤러에서 처리
        // startDate 또는 endDate 가 없는 경우
        if (startDate == null || endDate == null) {
            throw new InvalidRequestException("입력되지 않은 날짜가 존재합니다.");
        }
        // startDate 보다 endDate 가 빠른 날짜인 경우
        // ex) startDate : 2024-11-15, endDate : 2024-11-14
        if (startDate.isAfter(endDate)) {
            throw new InvalidRequestException("종료날짜가 시작날짜보다 빠를 수 없습니다.");
        }
    }
    // LocalDate 형식의 날짜 데이터를 LocalDateTime 형식으로 변환
    public LocalDateTime convertToStartDateTime (LocalDate startDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        return startDateTime;
    }
    public LocalDateTime convertToEndDateTime (LocalDate endDate) {
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        return endDateTime;
    }

    public TodoResponse getTodo(long todoId) {
        Todo todo = todoRepository.findByIdWithUser(todoId)
                .orElseThrow(() -> new InvalidRequestException("Todo not found"));

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
}
