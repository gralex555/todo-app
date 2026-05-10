package com.todoproject.todo_app.mapper;

import com.todoproject.todo_app.dto.TaskRequestDTO;
import com.todoproject.todo_app.dto.TaskResponseDTO;
import com.todoproject.todo_app.entity.Task;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TaskMapper {

    public Task toEntity(TaskRequestDTO requestDTO) { // Преобразует RequestDto в Entity (при создании или обновлении задачи)
        if (requestDTO == null) {
            return null;
        }

        return Task.builder()
                .title(requestDTO.getTitle())
                .description(requestDTO.getDescription())
                .completed(requestDTO.getCompleted() != null ? requestDTO.getCompleted() : false)
                .build();
    }

    public TaskResponseDTO toResponseDTO(Task task) {  // преобразуем Entity в ResponseDTO
        if (task == null) {
            return null;
        }

        return TaskResponseDTO.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .completed(task.getCompleted())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

    public List<TaskResponseDTO> toResponseDTOList(List<Task> tasks) {  // Преобразует список Entity в список ResponseDto
        return tasks.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public void updateEntityFromDTO(TaskRequestDTO requestDTO, Task task) {  // Обновляет существующую Entity данными из RequestDto (для PUT/PATCH)
        if (requestDTO == null || task == null) {
            return;
        }

        task.setTitle(requestDTO.getTitle());
        task.setDescription(requestDTO.getDescription());

        if (requestDTO.getCompleted() != null) {
            task.setCompleted(requestDTO.getCompleted());
        }
    }
}
