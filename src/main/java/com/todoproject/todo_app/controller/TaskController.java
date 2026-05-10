package com.todoproject.todo_app.controller;

import com.todoproject.todo_app.dto.TaskRequestDTO;
import com.todoproject.todo_app.dto.TaskResponseDTO;
import com.todoproject.todo_app.entity.Task;
import com.todoproject.todo_app.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Управление задачами")
public class TaskController {
    private final TaskService taskService;

    // ====================== CRUD ======================

    @Operation(summary = "Создать задачу")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Задача создана"),
            @ApiResponse(responseCode = "400", description = "Неверные входные данные"),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    @PostMapping
    public ResponseEntity<TaskResponseDTO> create(@Valid @RequestBody TaskRequestDTO requestDTO) {
        TaskResponseDTO createdTask = taskService.create(requestDTO);
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }

    @Operation(summary = "Получить задачу по ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Задача найдена"),
            @ApiResponse(responseCode = "404", description = "Задача не найдена"),
            @ApiResponse(responseCode = "403", description = "Нет доступа к этой задаче")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> getById(@PathVariable Long id) {
        TaskResponseDTO task = taskService.getById(id);
        return ResponseEntity.ok(task);
    }

    @Operation(summary = "Обновить задачу")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Задача обновлена"),
            @ApiResponse(responseCode = "404", description = "Задача не найдена"),
            @ApiResponse(responseCode = "403", description = "Нет доступа к этой задаче")
    })
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> update(@PathVariable Long id,
                                                  @Valid @RequestBody TaskRequestDTO requestDTO) {
        TaskResponseDTO updatedTask = taskService.update(id, requestDTO);
        return ResponseEntity.ok(updatedTask);
    }

    @Operation(summary = "Удалить задачу")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Задача удалена"),
            @ApiResponse(responseCode = "404", description = "Задача не найдена"),
            @ApiResponse(responseCode = "403", description = "Нет доступа к этой задаче")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ====================== Пагинация ======================

    @Operation(summary = "Получить все задачи с пагинацией")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список задач"),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    @GetMapping
    public ResponseEntity<Page<TaskResponseDTO>> getAllPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        Page<TaskResponseDTO> tasks = taskService.getAllPaginated(page, size, sort);
        return ResponseEntity.ok(tasks);
    }

    @Operation(summary = "Получить задачи по статусу выполнения")
    @GetMapping("/completed")
    public ResponseEntity<Page<TaskResponseDTO>> getByCompletedPaginated(
            @RequestParam boolean completed,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        Page<TaskResponseDTO> tasks = taskService.getByCompletedPaginated(completed, page, size, sort);
        return ResponseEntity.ok(tasks);
    }

    @Operation(summary = "Поиск задач по названию")
    @GetMapping("/search")
    public ResponseEntity<Page<TaskResponseDTO>> searchByTitlePaginated(
            @RequestParam String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        Page<TaskResponseDTO> tasks = taskService.searchByTitlePaginated(title, page, size, sort);
        return ResponseEntity.ok(tasks);
    }

    // тестовые запросы
    // /api/tasks?page=0&size=10&sort=createdAt,desc
    // /api/tasks?page=0&size=10&sort=title,asc
    // /api/tasks/completed?completed=true&page=0&size=5&sort=updatedAt,desc

    // ====================== Методы без пагинации (для совместимости) ======================

    @Operation(summary = "Получить все задачи без пагинации")
    @GetMapping("/all")
    public ResponseEntity<List<TaskResponseDTO>> getAll() {
        List<TaskResponseDTO> tasks = taskService.getAll();
        return ResponseEntity.ok(tasks);
    }




}
