package com.todoproject.todo_app.controller;

import com.todoproject.todo_app.dto.TaskResponseDTO;
import com.todoproject.todo_app.exception.TaskNotFoundException;
import com.todoproject.todo_app.security.JwtTokenProvider;
import com.todoproject.todo_app.service.TaskService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.todoproject.todo_app.dto.TaskRequestDTO;
import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc
@DisplayName("Тесты TaskController")
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Nested
    @DisplayName("GET /api/tasks/{id}")
    class GetByIdTests {

        @Test
        @DisplayName("возвращает задачу со статусом 200 при успешном запросе")
        @WithMockUser(username = "testuser")
        void shouldReturnTaskWhenIdExists() throws Exception {
            // given
            Long taskId = 1L;
            TaskResponseDTO taskDto = new TaskResponseDTO();
            taskDto.setId(taskId);
            taskDto.setTitle("Купить хлеб");

            when(taskService.getById(taskId)).thenReturn(taskDto);

            // when & then
            mockMvc.perform(get("/api/tasks/{id}", taskId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").value("Купить хлеб"));
        }

        @Test
        @DisplayName("Возвращает ошибку 404 если задача не найдена")
        @WithMockUser(username = "testuser")
        void shouldReturn404WhenTaskNotFound() throws Exception {
            // given
            Long nonExistentId = 999L;

            when(taskService.getById(nonExistentId))
                    .thenThrow(new TaskNotFoundException(999L));

            // when & then
            mockMvc.perform(get("/api/tasks/{id}", nonExistentId))
                    .andExpect(status().isNotFound());

        }

        @Test
        @DisplayName("Возвращает ошибку 403 если задача принадлежит другому пользователю")
        @WithMockUser(username = "testuser")
        void shouldReturn403WhenTaskIsNotOurs() throws Exception {

            // given
            Long taskId = 9L;

            when(taskService.getById(taskId))
                    .thenThrow(new AccessDeniedException("Нет доступа к этой задаче"));

            // when & then
            mockMvc.perform(get("/api/tasks/{id}", taskId))
                    .andExpect(status().isForbidden());  // "Я ожидаю, что HTTP-ответ имеет статус 403 (Forbidden)."

        }

    }

    @Nested
    @DisplayName("CREATE /api/tasks/{id}")
    class CreateTests {
        @Test
        @DisplayName("Создаёт задачу и возвращает 201 с DTO")
        @WithMockUser(username = "testuser")
        void shouldCreateTaskAndReturn201() throws Exception {
            // given
            TaskResponseDTO savedDto = new TaskResponseDTO();
            savedDto.setId(1L);
            savedDto.setTitle("Купить хлеб");

            when(taskService.create(any(TaskRequestDTO.class))).thenReturn(savedDto);

            String requestJson = """
            {
                "title": "Купить хлеб",
                "description": "В Буше",
                "completed": false
            }
            """;

            // when & then
            mockMvc.perform(post("/api/tasks")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").value("Купить хлеб"));
        }

        @Test
        @DisplayName("Возвращает ошибку 400 если title пустой")
        @WithMockUser(username = "testuser")
        void shouldReturn400WhenTitleIsBlank() throws Exception {

            String requestJson = """
            {
                "title": "",
                "description": "В Кафе",
                "completed": false
            }
            """;

            // when & then
            mockMvc.perform(post("/api/tasks")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isBadRequest());
        }


    }

    @Nested
    @DisplayName("DELETE /api/tasks/{id}")
    class DeleteTests {

        @Test
        @DisplayName("Удаляет задачу и возвращает ответ 204")
        @WithMockUser(username = "testuser")
        void shouldDeleteTaskAndReturn204() throws Exception {
            Long taskId = 9L;

            mockMvc.perform(
                            delete("/api/tasks/{id}", taskId)
                                    .with(csrf())
                    )
                    .andExpect(status().isNoContent());

            verify(taskService).delete(taskId);
        }

        @Test
        @DisplayName("Возвращает 404 если задача не найдена при удалении")
        @WithMockUser(username = "testuser")
        void shouldReturn404WhenTaskNotFoundWhenDeleting() throws Exception {

            Long nonExistentId = 999L;

            doThrow(new TaskNotFoundException(nonExistentId))
                    .when(taskService).delete(nonExistentId);

            mockMvc.perform(
                            delete("/api/tasks/{id}", nonExistentId)
                                    .with(csrf())
                    )
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Возвращает 403 если задача принадлежит другому пользователю при удалении")
        @WithMockUser(username = "testuser")
        void shouldReturn403WhenTaskBelongsToAnotherUserOnDelete() throws Exception {
            Long taskId = 9L;

            doThrow(new AccessDeniedException("Нет доступа к этой задаче"))
                    .when(taskService).delete(taskId);


            // when & then
            mockMvc.perform(
                            delete("/api/tasks/{id}", taskId)
                                    .with(csrf())
                    )
                    .andExpect(status().isForbidden());
        }

    }

    @Nested
    @DisplayName("UPDATE /api/tasks/{id}")
    class UpdateTests {

        @Test
        @DisplayName("Обновляет задачу и возвращает ответ 200 с обновленным DTO")
        @WithMockUser(username = "testuser")
        void shouldUpdateTaskAndReturn200() throws Exception {

            TaskResponseDTO updatedDto = new TaskResponseDTO();
            updatedDto.setId(1L);
            updatedDto.setTitle("Купить хлеб");
            Long taskId = 1L;

            when(taskService.update(eq(taskId), any(TaskRequestDTO.class))).thenReturn(updatedDto);

            String requestJson = """
            {
                "title": "Купить хлеб",
                "description": "В Буше",
                "completed": false
            }
            """;

            // when & then
            mockMvc.perform(put("/api/tasks/{id}", taskId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").value("Купить хлеб"));

        }

        @Test
        @DisplayName("Возвращает 404 если задача не найдена при обновлении")
        @WithMockUser(username = "testuser")
        void shouldReturn404WhenTaskNotFoundWhenUpdating() throws Exception {

            Long nonExistentId = 999L;

            when(taskService.update(eq(nonExistentId), any(TaskRequestDTO.class)))
                    .thenThrow(new TaskNotFoundException(nonExistentId));

            String requestJson = """
            {
                "title": "Купить хлеб",
                "description": "В Буше",
                "completed": false
            }
            """;

            mockMvc.perform(put("/api/tasks/{id}", nonExistentId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Возвращает 403 если задача принадлежит другому пользователю при обновлении")
        @WithMockUser(username = "testuser")
        void shouldReturn403WhenTaskBelongsToAnotherUserOnUpdate() throws Exception {

            Long taskId = 9L;

            when(taskService.update(eq(taskId), any(TaskRequestDTO.class)))
                    .thenThrow(new AccessDeniedException("Нет доступа к этой задаче"));

            String requestJson = """
            {
                "title": "Купить хлеб",
                "description": "В Буше",
                "completed": false
            }
            """;

            mockMvc.perform(put("/api/tasks/{id}", taskId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isForbidden());

        }

        @Test
        @DisplayName("Возвращает ошибку 400 если title пустой при обновлении")
        @WithMockUser(username = "testuser")
        void shouldReturn400WhenTitleIsBlankWhenUpdating() throws Exception {
            TaskResponseDTO updatedDto = new TaskResponseDTO();
            updatedDto.setId(1L);
            updatedDto.setTitle("Купить хлеб");
            Long taskId = 1L;

            when(taskService.update(eq(taskId), any(TaskRequestDTO.class))).thenReturn(updatedDto);

            String requestJson = """
            {
                "title": "",
                "description": "В Кафе",
                "completed": false
            }
            """;

            mockMvc.perform(put("/api/tasks/{id}", taskId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isBadRequest());
        }

    }


}