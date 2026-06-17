package com.todoproject.todo_app.event;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TaskCreatedEvent {
    private Long taskId;
    private Long userId;
    private String title;
    private LocalDateTime occurredAt;
}
