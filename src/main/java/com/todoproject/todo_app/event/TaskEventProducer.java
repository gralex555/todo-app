package com.todoproject.todo_app.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskEventProducer {
    private static final String TOPIC = "task-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendTaskCreated(TaskCreatedEvent event) {
        log.info("Sending TaskCreatedEvent to Kafka: {}", event);
        kafkaTemplate.send(TOPIC, event.getTaskId().toString(), event);
    }

    public void sendTaskUpdated(TaskUpdatedEvent event) {
        log.info("Sending TaskUpdatedEvent to Kafka: {}", event);
        kafkaTemplate.send(TOPIC, event.getTaskId().toString(), event);
    }

    public void sendTaskDeleted(TaskDeletedEvent event) {
        log.info("Sending TaskDeletedEvent to Kafka: {}", event);
        kafkaTemplate.send(TOPIC, event.getTaskId().toString(), event);
    }
}
