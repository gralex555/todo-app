package com.todoproject.todo_app.service;

import com.todoproject.todo_app.dto.TaskRequestDTO;
import com.todoproject.todo_app.dto.TaskResponseDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface TaskService {

    TaskResponseDTO create(TaskRequestDTO requestDTO);

    TaskResponseDTO getById(Long id);

    void delete(Long id);

    TaskResponseDTO update(Long id, TaskRequestDTO requestDTO);



    // Пагинация и сортировка
    Page<TaskResponseDTO> getAllPaginated(int page, int size, String sort);

    Page<TaskResponseDTO> getByCompletedPaginated(boolean completed, int page, int size, String sort);

    Page<TaskResponseDTO> searchByTitlePaginated(String title, int page, int size, String sort);


    // === Методы без пагинации ===
    List<TaskResponseDTO> getAll();

    List<TaskResponseDTO> getByCompleted(boolean completed);

    List<TaskResponseDTO> searchByTitle(String title);
}
