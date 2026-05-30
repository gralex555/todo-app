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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
                    .andExpect(status().isForbidden());

        }

    }


}