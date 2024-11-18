package org.example.expert.domain.todo.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.example.expert.domain.todo.entity.QTodo;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.user.entity.QUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class TodoRepositoryQueryImpl implements TodoRepositoryQuery {

    private final JPAQueryFactory queryFactory;
    private final QTodo todo = QTodo.todo;
    private final QUser user = QUser.user;

    public TodoRepositoryQueryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    // weather 조건과 기간 조건이 모두 있는 경우
    @Override
    public Page<Todo> findByWeatherAndDate(String weather, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        List<Todo> todos = queryFactory
                .selectFrom(todo)
                .leftJoin(todo.user, user).fetchJoin()
                .where(todo.weather.eq(weather).and(todo.modifiedAt.between(startDate, endDate)))
                .orderBy(todo.modifiedAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        // 페이징 처리를 위한 조회된 일정을 카운트
        long total = Optional.ofNullable(queryFactory
                        .select(todo.count())
                        .from(todo)
                        .where(todo.weather.eq(weather).and(todo.modifiedAt.between(startDate, endDate)))
                        .fetchOne())
                .orElse(0L);

        return new PageImpl<>(todos, pageable, total);
    }

    // weather  조건만 있는 경우
    @Override
    public Page<Todo> findByWeatherOrOrderByModifiedAt(String weather, Pageable pageable) {
        List<Todo> todos = queryFactory
                .selectFrom(todo)
                .leftJoin(todo.user, user).fetchJoin()
                .where(todo.weather.eq(weather))
                .orderBy(todo.modifiedAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        // 페이징 처리를 위한 조회된 일정을 카운트
        long total = Optional.ofNullable(queryFactory
                        .select(todo.count())
                        .from(todo)
                        .where(todo.weather.eq(weather))
                        .fetchOne())
                .orElse(0L);

        return new PageImpl<>(todos, pageable, total);
    }

    // 기간 조건만 있는 경우
    @Override
    public Page<Todo> findByDateOrderByModifiedAt(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        List<Todo> todos = queryFactory
                .selectFrom(todo)
                .leftJoin(todo.user, user).fetchJoin()
                .where(todo.modifiedAt.between(startDate, endDate))
                .orderBy(todo.modifiedAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        // 페이징 처리를 위한 조회된 일정을 카운트
        long total = Optional.ofNullable(queryFactory
                        .select(todo.count())
                        .from(todo)
                        .where(todo.modifiedAt.between(startDate, endDate))
                        .fetchOne())
                .orElse(0L);

        return new PageImpl<>(todos, pageable, total);
    }

    // 아무 조건도 없는 경우 전체 조회
    @Override
    public Page<Todo> findAllByOrderByModifiedAtDesc(Pageable pageable) {
        List<Todo> todos = queryFactory
                .selectFrom(todo)
                .leftJoin(todo.user, user).fetchJoin()
                .orderBy(todo.modifiedAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        // 페이징 처리를 위한 조회된 일정을 카운트
        long total = Optional.ofNullable(queryFactory
                        .select(todo.count())
                        .from(todo)
                        .fetchOne())
                .orElse(0L);

        return new PageImpl<>(todos, pageable, total);
    }

    // 특정 일정 조회
    public Optional<Todo> findByIdWithUser(Long todoId) {
        Todo optionalTodo = queryFactory
                .selectFrom(todo)
                .leftJoin(todo.user, user).fetchJoin()
                .where(todo.id.eq(todoId))
                .fetchOne();

        return Optional.ofNullable(optionalTodo);
    }
}
