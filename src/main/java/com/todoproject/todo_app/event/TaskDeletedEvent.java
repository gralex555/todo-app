package com.todoproject.todo_app.event;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TaskDeletedEvent {
    private Long taskId;
    private Long userId;
    private LocalDateTime occurredAt;
}
