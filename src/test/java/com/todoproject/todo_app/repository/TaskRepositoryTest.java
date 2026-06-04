package com.todoproject.todo_app.repository;

import com.todoproject.todo_app.entity.Task;
import com.todoproject.todo_app.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DisplayName("Тесты TaskRepository")
public class TaskRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TaskRepository taskRepository;

    @Test
    @DisplayName("findByOwner возвращает все задачи указанного пользователя")
    void shouldReturnAllTasksForGivenOwner() {
        // given
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .build();
        entityManager.persist(user);

        Task task1 = Task.builder()
                .title("Купить хлеб")
                .completed(false)
                .owner(user)
                .build();
        entityManager.persist(task1);

        Task task2 = Task.builder()
                .title("Помыть посуду")
                .completed(false)
                .owner(user)
                .build();
        entityManager.persist(task2);

        entityManager.flush();

        // when
        List<Task> result = taskRepository.findByOwner(user);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Task::getTitle)
                .containsExactlyInAnyOrder("Купить хлеб", "Помыть посуду");


    }

    @Test
    @DisplayName("findByOwner не должен возвращать задачи другого пользователя")
    void shouldNotReturnTasksOfOtherUsers() {

        User user1 = User.builder()
                .username("testuser1")
                .email("test1@example.com")
                .password("password")
                .build();
        entityManager.persist(user1);

        User user2 = User.builder()
                .username("testuser2")
                .email("test2@example.com")
                .password("password")
                .build();
        entityManager.persist(user2);

        Task task1 = Task.builder()
                .title("Купить хлеб")
                .completed(false)
                .owner(user1)
                .build();
        entityManager.persist(task1);

        Task task2 = Task.builder()
                .title("Купить овощи")
                .completed(false)
                .owner(user2)
                .build();
        entityManager.persist(task2);

        entityManager.flush();

        // when
        List<Task> result = taskRepository.findByOwner(user1);

        assertThat(result).extracting(Task::getTitle)
                .containsExactly("Купить хлеб");

    }

    @Test
    @DisplayName("findByOwnerAndCompleted должен возвращать только выполненные задачи")
    void shouldFindByOwnerAndCompletedShouldReturnOnlyCompletedTasks() {
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .build();
        entityManager.persist(user);

        Task task1 = Task.builder()
                .title("Купить хлеб")
                .completed(true)
                .owner(user)
                .build();
        entityManager.persist(task1);

        Task task2 = Task.builder()
                .title("Купить овощи")
                .completed(true)
                .owner(user)
                .build();
        entityManager.persist(task2);

        Task task3 = Task.builder()
                .title("Купить печенье")
                .completed(false)
                .owner(user)
                .build();
        entityManager.persist(task3);

        entityManager.flush();

        // when
        List<Task> result = taskRepository.findByOwnerAndCompleted(user, true);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Task::getTitle)
                .containsExactlyInAnyOrder("Купить хлеб", "Купить овощи");



    }

    @Test
    @DisplayName("findByOwnerAndTitleContainingIgnoreCase должен вернуть две первые задачи, но не третью задачу")
    void shouldReturnOnlyTwoTasks() {
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .build();
        entityManager.persist(user);

        Task task1 = Task.builder()
                .title("Купить хлеб")
                .completed(false)
                .owner(user)
                .build();
        entityManager.persist(task1);

        Task task2 = Task.builder()
                .title("Купить ХЛЕБ")
                .completed(false)
                .owner(user)
                .build();
        entityManager.persist(task2);

        Task task3 = Task.builder()
                .title("Купить печенье")
                .completed(false)
                .owner(user)
                .build();
        entityManager.persist(task3);

        entityManager.flush();

        List<Task> result = taskRepository.findByOwnerAndTitleContainingIgnoreCase(user, "хлеб");

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Task::getTitle)
                .containsExactlyInAnyOrder("Купить хлеб", "Купить ХЛЕБ");


    }

    @Test
    @DisplayName("findByOwner должен возвращать пустой список")
    void shouldReturnEmptyListWhenOwnerHasNoTasks() {

        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .build();
        entityManager.persist(user);

        entityManager.flush();

        List<Task> result = taskRepository.findByOwner(user);

        assertThat(result).isNotNull().isEmpty();


    }
}
