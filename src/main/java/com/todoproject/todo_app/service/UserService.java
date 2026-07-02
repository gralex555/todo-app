package com.todoproject.todo_app.service;

import com.todoproject.todo_app.dto.AuthResponseDTO;
import com.todoproject.todo_app.dto.LoginRequestDTO;
import com.todoproject.todo_app.dto.RegisterRequestDTO;
import com.todoproject.todo_app.entity.User;
/**
 * user service
 */
public interface UserService {

    /**
     * Register and save user
     * @param requestDTO this DTO with important information
     * @return updated user response
     */

    AuthResponseDTO register(RegisterRequestDTO requestDTO);

     // Аутентификация пользователя (логин)
    AuthResponseDTO login(LoginRequestDTO requestDTO);

    AuthResponseDTO refresh(String refreshToken);

    void logout(String refreshToken);

    // Найти пользователя по username
    User getUserByUsername(String username);
}
