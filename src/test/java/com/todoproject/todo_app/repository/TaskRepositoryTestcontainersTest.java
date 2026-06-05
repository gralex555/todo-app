package com.todoproject.todo_app.repository;

import com.todoproject.todo_app.entity.Task;
import com.todoproject.todo_app.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@DisplayName("Тесты TaskRepository с Testcontainers и реальной PostgreSQL")
public class TaskRepositoryTestcontainersTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.liquibase.enabled", () -> "true");
    }

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TaskRepository taskRepository;

    @Test
    @DisplayName("findByOwner возвращает все задачи указанного пользователя")
    void shouldReturnAllTasksForGivenOwner() {
        // given
        User user = User.builder()
                .username("testuser_for_repo_test")
                .email("testrepo@example.com")
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
                .username("testuser_for_repo_test1")
                .email("testrepo1@example.com")
                .password("password")
                .build();
        entityManager.persist(user1);

        User user2 = User.builder()
                .username("testuser2_for_repo_test2")
                .email("testrepo2@example.com")
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
                .username("testuser_for_repo_test")
                .email("testrepo@example.com")
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
                .username("testuser_for_repo_test")
                .email("testrepo@example.com")
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
                .username("testuser_for_repo_test")
                .email("testrepo@example.com")
                .password("password")
                .build();
        entityManager.persist(user);

        entityManager.flush();

        List<Task> result = taskRepository.findByOwner(user);

        assertThat(result).isNotNull().isEmpty();


    }
}
