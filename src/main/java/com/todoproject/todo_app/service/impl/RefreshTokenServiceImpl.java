package com.todoproject.todo_app.service.impl;

import com.todoproject.todo_app.entity.RefreshToken;
import com.todoproject.todo_app.entity.User;
import com.todoproject.todo_app.repository.RefreshTokenRepository;
import com.todoproject.todo_app.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-expiration:604800000}") // 7 дней по умолчанию
    private long refreshExpirationInMs;

    @Override
    public String createRefreshToken(User user) {  // Создать новый refresh токен для пользователя
        // Отзываем старые токены пользователя
        revokeAllUserTokens(user);

        // Генерируем случайную строку как токен
        String tokenValue = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenValue)
                .user(user)
                .expiryDate(LocalDateTime.now()
                        .plusSeconds(refreshExpirationInMs / 1000))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);

        return tokenValue;
    }

    @Override
    public User validateRefreshToken(String token) {  // Проверить валидность refresh токена и вернуть пользователя
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        // Проверяем что токен не отозван
        if (refreshToken.isRevoked()) {
            throw new RuntimeException("Refresh token has been revoked");
        }

        // Проверяем что токен не истёк
        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            revokeRefreshToken(token);
            throw new RuntimeException("Refresh token has expired");
        }

        return refreshToken.getUser();
    }

    @Override
    public void revokeRefreshToken(String token) {   // Отозвать токен (при logout)
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }

    @Override
    public void revokeAllUserTokens(User user) {
        List<RefreshToken> tokens = refreshTokenRepository.findByUser(user);
        tokens.forEach(token -> token.setRevoked(true));
        refreshTokenRepository.saveAll(tokens);
    }
}
