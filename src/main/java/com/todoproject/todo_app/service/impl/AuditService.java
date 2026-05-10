package com.todoproject.todo_app.service.impl;

import com.todoproject.todo_app.entity.AuditAction;
import com.todoproject.todo_app.entity.AuditLog;
import com.todoproject.todo_app.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Async
    public void logAction(Long userId,
                          AuditAction action,
                          String entityType,
                          Long entityId,
                          String details) {
        log.info("Saving audit log in thread: {}", Thread.currentThread().getName());

        AuditLog entry = AuditLog.builder()
                .userId(userId)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .build();
        auditLogRepository.save(entry);

    }
}
