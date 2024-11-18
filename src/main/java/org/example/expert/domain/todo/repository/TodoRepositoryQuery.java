package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TodoRepositoryQuery {

    // weather 조건과 기간 조건이 모두 있는 경우
    Page<Todo> findByWeatherAndDate(String weather, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // weather  조건만 있는 경우
    Page<Todo> findByWeatherOrOrderByModifiedAt(String weather, Pageable pageable);

    // 기간 조건만 있는 경우
    Page<Todo> findByDateOrderByModifiedAt(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // 아무 조건도 없는 경우 전체 조회
    Page<Todo> findAllByOrderByModifiedAtDesc(Pageable pageable);

    // 특정 일정 조회
    Optional<Todo> findByIdWithUser(Long todoId);
}
