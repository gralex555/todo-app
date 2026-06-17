package com.todoproject.todo_app.event;

import com.todoproject.todo_app.entity.AuditAction;
import com.todoproject.todo_app.service.impl.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@KafkaListener(
        topics = "task-events",
        groupId = "audit-group",
        containerFactory = "taskEventListenerContainerFactory"
)
public class TaskEventConsumer {
    private final AuditService auditService;

    @KafkaHandler
    public void onTaskCreated(TaskCreatedEvent event) {
        log.info("Received TaskCreatedEvent from Kafka: {}", event);

        auditService.logAction(
                event.getUserId(),
                AuditAction.CREATE_TASK,
                "Task",
                event.getTaskId(),
                "Создана задача: " + event.getTitle()
        );
    }

   @KafkaHandler
    public void onTaskUpdated(TaskUpdatedEvent event) {
        log.info("Received TaskUpdatedEvent from Kafka: {}", event);

        auditService.logAction(
                event.getUserId(),
                AuditAction.UPDATE_TASK,
                "Task",
                event.getTaskId(),
                "Обновлена задача: " + event.getTitle()
        );
    }

    @KafkaHandler
    public void onTaskDeleted(TaskDeletedEvent event) {
        log.info("Received TaskDeletedEvent from Kafka: {}", event);

        auditService.logAction(
                event.getUserId(),
                AuditAction.DELETE_TASK,
                "Task",
                event.getTaskId(),
                "Удалена задача с id: " + event.getTaskId()
        );
    }
}
