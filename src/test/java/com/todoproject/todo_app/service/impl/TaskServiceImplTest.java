package com.todoproject.todo_app.service.impl;

import com.todoproject.todo_app.dto.TaskRequestDTO;
import com.todoproject.todo_app.dto.TaskResponseDTO;
import com.todoproject.todo_app.entity.AuditAction;
import com.todoproject.todo_app.entity.Task;
import com.todoproject.todo_app.entity.User;
import com.todoproject.todo_app.exception.TaskNotFoundException;
import com.todoproject.todo_app.mapper.TaskMapper;
import com.todoproject.todo_app.repository.TaskRepository;
import com.todoproject.todo_app.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
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

    @Nested
    @DisplayName("Метод create")
    class CreateTests {

        @Test
        @DisplayName("возвращает DTO с правильными полями при создании задачи")
        void shouldReturnDtoWhenTaskIsCreated() {
            TaskRequestDTO requestDTO = new TaskRequestDTO();
            requestDTO.setTitle("Купить хлеб");

            Task taskFromMapper = new Task();
            taskFromMapper.setTitle("Купить хлеб");

            Task savedTask = new Task();
            savedTask.setId(10L);
            savedTask.setTitle("Купить хлеб");
            savedTask.setOwner(currentUser);

            TaskResponseDTO expectedDto = new TaskResponseDTO();
            expectedDto.setId(10L);
            expectedDto.setTitle("Купить хлеб");

            when(userRepository.findByUsername("testuser"))
                    .thenReturn(Optional.of(currentUser));
            when(taskMapper.toEntity(requestDTO))
                    .thenReturn(taskFromMapper);
            when(taskRepository.save(taskFromMapper))
                    .thenReturn(savedTask);
            when(taskMapper.toResponseDTO(savedTask))
                    .thenReturn(expectedDto);

            // when
            TaskResponseDTO result = taskService.create(requestDTO);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(10L);
            assertThat(result.getTitle()).isEqualTo("Купить хлеб");
            verify(auditService).logAction(any(), any(), any(), any(), any());

        }

        @Test
        @DisplayName("при создании задачи аудит вызывается с правильным действием и описанием")
        void shouldCallAuditWithCorrectArgumentsWhenTaskIsCreated() {
            TaskRequestDTO requestDTO = new TaskRequestDTO();
            requestDTO.setTitle("Купить хлеб");

            Task taskFromMapper = new Task();
            taskFromMapper.setTitle("Купить хлеб");

            Task savedTask = new Task();
            savedTask.setId(10L);
            savedTask.setTitle("Купить хлеб");
            savedTask.setOwner(currentUser);

            TaskResponseDTO expectedDto = new TaskResponseDTO();
            expectedDto.setId(10L);

            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(currentUser));
            when(taskMapper.toEntity(requestDTO)).thenReturn(taskFromMapper);
            when(taskRepository.save(taskFromMapper)).thenReturn(savedTask);
            when(taskMapper.toResponseDTO(savedTask)).thenReturn(expectedDto);

            ArgumentCaptor<AuditAction> actionCaptor = ArgumentCaptor.forClass(AuditAction.class);
            ArgumentCaptor<String> descriptionCaptor = ArgumentCaptor.forClass(String.class);

            // when
            taskService.create(requestDTO);

            // then
            verify(auditService).logAction(
                    any(),
                    actionCaptor.capture(),
                    any(),
                    any(),
                    descriptionCaptor.capture()
            );

            assertThat(actionCaptor.getValue()).isEqualTo(AuditAction.CREATE_TASK);
            assertThat(descriptionCaptor.getValue()).contains("Купить хлеб");
        }
    }

    @Nested
    @DisplayName("Метод delete")
    class deleteTests {

        @Test
        @DisplayName("бросает TaskNotFoundException, когда задача не найдена")
        void shouldThrowTaskNotFoundExceptionWhenTaskDoesNotExist() {
            // given
            Long nonExistentId = 999L;

            when(userRepository.findByUsername("testuser"))
                    .thenReturn(Optional.of(currentUser));
            when(taskRepository.findById(nonExistentId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> taskService.delete(nonExistentId))
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
            task.setTitle("Купить овощи");
            task.setOwner(taskOwner);


            when(taskRepository.findById(taskId))
                    .thenReturn(Optional.of(task));
            when(userRepository.findByUsername("testuser"))
                    .thenReturn(Optional.of(currentUser));

            assertThatThrownBy(() -> taskService.delete(taskId))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        @DisplayName("удаляет задачу")
        void shouldDeleteTask() {
            // given
            Long taskId = 2L;

            Task task = new Task();
            task.setId(taskId);
            task.setTitle("Купить овощи");
            task.setOwner(currentUser);

            when(userRepository.findByUsername("testuser"))
                    .thenReturn(Optional.of(currentUser));
            when(taskRepository.findById(taskId))
                    .thenReturn(Optional.of(task));

            // when
            taskService.delete(taskId);

            // then
            verify(taskRepository).deleteById(taskId);
            verify(auditService).logAction(any(), any(), any(), any(), any());

        }

    }

    @Nested
    @DisplayName("Метод update")
    class updateTests {

        @Test
        @DisplayName("возвращает DTO, когда задача найдена и принадлежит текущему пользователю")
        void shouldReturnDtoWhenTaskExistsAndBelongsToCurrentUser() {

            Long taskId = 2L;

            TaskRequestDTO requestDTO = new TaskRequestDTO();
            requestDTO.setTitle("aa");
            requestDTO.setDescription("bb");
            requestDTO.setCompleted(false);

            Task task = new Task();
            task.setId(taskId);
            task.setTitle("Купить овощи");
            task.setOwner(currentUser);

            Task updatedTask = new Task();
            updatedTask.setId(taskId);
            updatedTask.setTitle("Купить хлеб");
            updatedTask.setOwner(currentUser);

            TaskResponseDTO expectedDto = new TaskResponseDTO();
            expectedDto.setId(taskId);
            expectedDto.setTitle("Купить хлеб");

            when(userRepository.findByUsername("testuser"))
                    .thenReturn(Optional.of(currentUser));
            when(taskRepository.findById(taskId))
                    .thenReturn(Optional.of(task));
            when(taskMapper.toResponseDTO(updatedTask))
                    .thenReturn(expectedDto);
            when(taskRepository.save(task)).thenReturn(updatedTask);

            TaskResponseDTO result = taskService.update(taskId, requestDTO);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(taskId);
            assertThat(result.getTitle()).isEqualTo("Купить хлеб");
            verify(taskRepository).save(task);
            verify(auditService).logAction(any(), any(), any(), any(), any());

        }

        @Test
        @DisplayName("бросает TaskNotFoundException, когда задача не найдена")
        void shouldThrowTaskNotFoundExceptionWhenTaskDoesNotExist() {
            // given
            Long nonExistentId = 998L;

            TaskRequestDTO requestDTO = new TaskRequestDTO();
            requestDTO.setTitle("a");
            requestDTO.setDescription("b");
            requestDTO.setCompleted(false);


            when(userRepository.findByUsername("testuser"))
                    .thenReturn(Optional.of(currentUser));
            when(taskRepository.findById(nonExistentId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> taskService.update(nonExistentId, requestDTO))
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
            task.setTitle("Купить овощи");
            task.setOwner(taskOwner);

            TaskRequestDTO requestDTO = new TaskRequestDTO();
            requestDTO.setTitle("aa");
            requestDTO.setDescription("bb");
            requestDTO.setCompleted(false);


            when(taskRepository.findById(taskId))
                    .thenReturn(Optional.of(task));
            when(userRepository.findByUsername("testuser"))
                    .thenReturn(Optional.of(currentUser));

            assertThatThrownBy(() -> taskService.update(taskId, requestDTO))
                    .isInstanceOf(AccessDeniedException.class);
        }

    }

}
