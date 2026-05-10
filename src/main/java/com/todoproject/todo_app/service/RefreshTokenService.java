package com.todoproject.todo_app.service;

import com.todoproject.todo_app.entity.User;

public interface RefreshTokenService {

    // Создать новый refresh токен для пользователя
    String createRefreshToken(User user);

    // Проверить валидность refresh токена и вернуть пользователя
    User validateRefreshToken(String token);

    // Отозвать токен (при logout)
    void revokeRefreshToken(String token);

    // Удалить все токены пользователя (при logout со всех устройств)
    void revokeAllUserTokens(User user);
}
