package com.todoproject.todo_app.service.impl;
import com.todoproject.todo_app.entity.AuditAction;
import com.todoproject.todo_app.dto.TaskRequestDTO;
import com.todoproject.todo_app.dto.TaskResponseDTO;
import com.todoproject.todo_app.entity.Task;
import com.todoproject.todo_app.entity.User;
import com.todoproject.todo_app.exception.TaskNotFoundException;
import com.todoproject.todo_app.mapper.TaskMapper;
import com.todoproject.todo_app.repository.TaskRepository;
import com.todoproject.todo_app.repository.UserRepository;
import com.todoproject.todo_app.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;
    private final AuditService auditService;

    // ====================== CRUD ======================

    @Override
    @Transactional
    public TaskResponseDTO create(TaskRequestDTO requestDTO) {
        User currentUser = getCurrentUser();

        Task task = taskMapper.toEntity(requestDTO);
        task.setOwner(currentUser);
        Task savedTask = taskRepository.save(task);

        auditService.logAction(
                currentUser.getId(),
                AuditAction.CREATE_TASK,
                "Task",
                savedTask.getId(),
                "Создана задача: " + savedTask.getTitle()
        );

        return taskMapper.toResponseDTO(savedTask);
    }

    @Override
    @Cacheable(value = "tasks", key = "#id")
    public TaskResponseDTO getById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        // Проверяем что задача принадлежит текущему пользователю
        if (!task.getOwner().getId().equals(getCurrentUser().getId())) {
            throw new AccessDeniedException("Нет доступа к этой задаче");
        }
        return taskMapper.toResponseDTO(task);
    }

    @Override
    @Transactional
    @CacheEvict(value = "tasks", key = "#id")
    public TaskResponseDTO update(Long id, TaskRequestDTO requestDTO) {
        User currentUser = getCurrentUser();

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        // Проверяем, что задача принадлежит текущему пользователю
        if (!task.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Нет доступа к этой задаче");
        }

        taskMapper.updateEntityFromDTO(requestDTO, task);
        Task updatedTask = taskRepository.save(task);

        auditService.logAction(
                currentUser.getId(),
                AuditAction.UPDATE_TASK,
                "Task",
                updatedTask.getId(),
                "Обновлена задача: " + updatedTask.getTitle()
        );


        return taskMapper.toResponseDTO(updatedTask);
    }

    @Override
    @Transactional
    @CacheEvict(value = "tasks", key = "#id")
    public void delete(Long id) {
        User currentUser = getCurrentUser();

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        // Проверяем что задача принадлежит текущему пользователю
        if (!task.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Нет доступа к этой задаче");
        }

        String taskTitle = task.getTitle();
        Long taskId = task.getId();

        taskRepository.deleteById(id);

        auditService.logAction(
                currentUser.getId(),
                AuditAction.DELETE_TASK,
                "Task",
                taskId,
                "Удалена задача: " + taskTitle
        );
    }


    // ===== вспомогательный метод
    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }


    // ====================== Пагинация с сортировкой ======================

    @Override
    public Page<TaskResponseDTO> getAllPaginated(int page, int size, String sort) {
        Pageable pageable = createPageable(page, size, sort);
        Page<Task> taskPage = taskRepository.findByOwner(getCurrentUser(), pageable);  // ← только свои
        return taskPage.map(taskMapper::toResponseDTO);
    }

    @Override
    public Page<TaskResponseDTO> getByCompletedPaginated(boolean completed, int page, int size, String sort) {
        Pageable pageable = createPageable(page, size, sort);
        Page<Task> taskPage = taskRepository.findByOwnerAndCompleted(getCurrentUser(), completed, pageable);  // ← только свои
        return taskPage.map(taskMapper::toResponseDTO);
    }

    @Override
    public Page<TaskResponseDTO> searchByTitlePaginated(String title, int page, int size, String sort) {
        Pageable pageable = createPageable(page, size, sort);
        Page<Task> taskPage = taskRepository.findByOwnerAndTitleContainingIgnoreCase(getCurrentUser(), title, pageable);  // ← только свои
        return taskPage.map(taskMapper::toResponseDTO);
    }



     Pageable createPageable(int page, int size, String sort) {
        // Значение по умолчанию: новые задачи сверху
        if (sort == null || sort.trim().isEmpty()) {
            sort = "createdAt,desc";
        }

        String[] sortParts = sort.split(",");
        String property = sortParts[0].trim();
        String directionStr = sortParts.length > 1 ? sortParts[1].trim().toUpperCase() : "DESC";

        Sort.Direction direction = "ASC".equals(directionStr)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return PageRequest.of(page, size, Sort.by(direction, property));
    }

    // ====================== Методы без пагинации ======================

    @Override
    public List<TaskResponseDTO> getAll() {
        User currentUser = getCurrentUser();
        List<Task> tasks = taskRepository.findByOwner(currentUser);  // ← только свои задачи
        return taskMapper.toResponseDTOList(tasks);
    }

    @Override
    public List<TaskResponseDTO> getByCompleted(boolean completed) {
        User currentUser = getCurrentUser();
        List<Task> tasks = taskRepository.findByOwnerAndCompleted(currentUser, completed);  // ← только свои
        return taskMapper.toResponseDTOList(tasks);
    }

    @Override
    public List<TaskResponseDTO> searchByTitle(String title) {
        User currentUser = getCurrentUser();
        List<Task> tasks = taskRepository.findByOwnerAndTitleContainingIgnoreCase(currentUser, title);  // ← только свои
        return taskMapper.toResponseDTOList(tasks);
    }


}
