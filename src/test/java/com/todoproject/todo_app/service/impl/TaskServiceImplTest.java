package com.todoproject.todo_app.service.impl;

import com.todoproject.todo_app.dto.TaskResponseDTO;
import com.todoproject.todo_app.entity.Task;
import com.todoproject.todo_app.entity.User;
import com.todoproject.todo_app.exception.TaskNotFoundException;
import com.todoproject.todo_app.mapper.TaskMapper;
import com.todoproject.todo_app.repository.TaskRepository;
import com.todoproject.todo_app.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты TaskServiceImpl")
class TaskServiceImplTest {
    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private TaskServiceImpl taskService;

    // Тестовые данные, общие для всех тестов
    private User currentUser;

    @BeforeEach
    void setUp() {
        // Создаём тестового пользователя
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("testuser");

        // Кладём его в SecurityContext (подделываем аутентификацию)
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("testuser", null)
        );
    }

    @AfterEach
    void tearDown() {
        // Очищаем SecurityContext после каждого теста, чтобы тесты не влияли друг на друга
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("Метод getById")
    class GetByIdTests {
        @Test
        @DisplayName("возвращает DTO, когда задача найдена и принадлежит текущему пользователю")
        void shouldReturnDtoWhenTaskExistsAndBelongsToCurrentUser() {
            // given — готовим данные и настраиваем моки
            Long taskId = 1L;

            Task task = new Task();
            task.setId(taskId);
            task.setTitle("Купить хлеб");
            task.setOwner(currentUser);

            TaskResponseDTO expectedDto = new TaskResponseDTO();
            expectedDto.setId(taskId);
            expectedDto.setTitle("Купить хлеб");

            when(userRepository.findByUsername("testuser"))
                    .thenReturn(Optional.of(currentUser));
            when(taskRepository.findById(taskId))
                    .thenReturn(Optional.of(task));
            when(taskMapper.toResponseDTO(task))
                    .thenReturn(expectedDto);

            // when — вызываем тестируемый метод
            TaskResponseDTO result = taskService.getById(taskId);

            // then — проверяем результат
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(taskId);
            assertThat(result.getTitle()).isEqualTo("Купить хлеб");
        }

        @Test
        @DisplayName("бросает TaskNotFoundException, когда задача не найдена")
        void shouldThrowTaskNotFoundExceptionWhenTaskDoesNotExist() {
            // given
            Long nonExistentId = 999L;

            when(taskRepository.findById(nonExistentId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> taskService.getById(nonExistentId))
                    .isInstanceOf(TaskNotFoundException.class);
        }

        @Test
        @DisplayName("бросает AccessDeniedException, когда задача принадлежит другому пользователю")
        void shouldThrowAccessDeniedExceptionWhenTaskIsNotCurrentUser() {
            User taskOwner = new User();
            taskOwner.setId(2L);
            Long taskId = 2L;

            Task task = new Task();
            task.setId(taskId);
            task.setTitle("Купить масло");
            task.setOwner(taskOwner);


            when(taskRepository.findById(taskId))
                    .thenReturn(Optional.of(task));
            when(userRepository.findByUsername("testuser"))
                    .thenReturn(Optional.of(currentUser));

            assertThatThrownBy(() -> taskService.getById(taskId))
                    .isInstanceOf(AccessDeniedException.class);
        }

    }

}
