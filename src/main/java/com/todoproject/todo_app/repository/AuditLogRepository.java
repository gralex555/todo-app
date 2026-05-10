package com.todoproject.todo_app.repository;

import com.todoproject.todo_app.entity.AuditAction;
import com.todoproject.todo_app.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // Все действия пользователя (с пагинацией)
    Page<AuditLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);


    // Действия по конкретной сущности (история одной задачи)
    List<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
            String entityType, Long entityId);


    // Сколько действий совершил пользователь
    long countByUserId(Long userId);

}
