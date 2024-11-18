package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TodoRepository extends JpaRepository<Todo, Long> {

    // weather 조건과 기간 조건이 모두 있는 경우
    @Query("select t from Todo t left join fetch t.user " +
            "where t.weather = :weather " +
            "and t.modifiedAt between :startDate and :endDate " +
            "order by t.modifiedAt desc")
    Page<Todo> findByWeatherAndDate(@Param("weather") String weather,
                                    @Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate,
                                    Pageable pageable);

    // weather  조건만 있는 경우
    @Query("select t from Todo t left join fetch t.user " +
            "where t.weather = :weather " +
            "order by t.modifiedAt desc")
    Page<Todo> findByWeatherOrOrderByModifiedAt(@Param("weather") String weather,
                                                Pageable pageable);

    // 기간 조건만 있는 경우
    @Query("select t from Todo t left join fetch t.user " +
            "where t.modifiedAt between :startDate and :endDate " +
            "order by t.modifiedAt desc")
    Page<Todo> findByDateOrderByModifiedAt(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate,
                                           Pageable pageable);

    // 아무 조건도 없는 경우 전체 조회
    @Query("SELECT t FROM Todo t LEFT JOIN FETCH t.user u ORDER BY t.modifiedAt DESC")
    Page<Todo> findAllByOrderByModifiedAtDesc(Pageable pageable);

    @Query("SELECT t FROM Todo t " +
            "LEFT JOIN t.user " +
            "WHERE t.id = :todoId")
    Optional<Todo> findByIdWithUser(@Param("todoId") Long todoId);
}
