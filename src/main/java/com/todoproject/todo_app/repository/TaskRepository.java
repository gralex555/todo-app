package com.todoproject.todo_app.repository;

import com.todoproject.todo_app.entity.Task;

import com.todoproject.todo_app.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.List;


@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // методы с фильтрацией по owner
    List<Task> findByOwner(User owner);  // все задачи текущего пользователя
    List<Task> findByOwnerAndCompleted(User owner, Boolean completed);   // Задачи текущего пользователя по статусу
    List<Task> findByOwnerAndTitleContainingIgnoreCase(User owner, String title);   // Поиск по названию у текущего пользователя


    Page<Task> findByOwner(User owner, Pageable pageable);   // Задачи текущего пользователя с пагинацией
    Page<Task> findByOwnerAndCompleted(User owner, Boolean completed, Pageable pageable);   // Задачи текущего пользователя по статусу
    Page<Task> findByOwnerAndTitleContainingIgnoreCase(User owner, String title, Pageable pageable);   // Поиск по названию у текущего пользователя

    // Все задачи текущего пользователя, отсортированные по дате создания (новые сверху)
    Page<Task> findByOwnerOrderByCreatedAtDesc(User owner, Pageable pageable);

    // ==================== Методы без пагинации ====================

    List<Task> findByCompleted(boolean completed);

    List<Task> findByTitleContainingIgnoreCase(String title);
}
