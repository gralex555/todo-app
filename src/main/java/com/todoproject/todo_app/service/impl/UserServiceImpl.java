package com.todoproject.todo_app.service.impl;

import com.todoproject.todo_app.dto.AuthResponseDTO;
import com.todoproject.todo_app.dto.LoginRequestDTO;
import com.todoproject.todo_app.dto.RegisterRequestDTO;
import com.todoproject.todo_app.entity.User;
import com.todoproject.todo_app.exception.UserAlreadyExistsException;
import com.todoproject.todo_app.repository.UserRepository;
import com.todoproject.todo_app.security.JwtTokenProvider;
import com.todoproject.todo_app.service.RefreshTokenService;
import com.todoproject.todo_app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    @Value("${jwt.expiration}")
    private long jwtExpirationInMs;

    @Override
    public AuthResponseDTO register(RegisterRequestDTO requestDTO) {
        // Проверяем, существует ли уже пользователь с таким username или email
        if (userRepository.existsByUsername(requestDTO.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists");
        }
        if (userRepository.existsByEmail(requestDTO.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists");
        }

        // Создаём нового пользователя
        User user = User.builder()
                .username(requestDTO.getUsername())
                .email(requestDTO.getEmail())
                .password(passwordEncoder.encode(requestDTO.getPassword()))  // шифруем пароль
                .build();

        user.addRole("ROLE_USER");   // даём роль по умолчанию
        User savedUser = userRepository.save(user);

        // Генерируем JWT токен
        String accessToken = jwtTokenProvider.generateToken(savedUser);
        String refreshToken = refreshTokenService.createRefreshToken(savedUser);

        return AuthResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .expiresAt(LocalDateTime.now().plusSeconds(jwtExpirationInMs / 1000))
                .build();
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO requestDTO) {
        try {

            // Аутентифицируем пользователя
            Authentication authentication = authenticationManager.authenticate(  // кладем в Менеджер непровер токен
                    new UsernamePasswordAuthenticationToken(   // создание непроверенного токена аутентификации, в который кладем непровер логин и пароль
                            requestDTO.getUsername(),
                            requestDTO.getPassword()
                    )
            );
// внктри AuthenticationManager - передает данные в AuthenticationProvider , а внутри него DaoAuthenticationProvider по username вызывает UserDetailsService.loadUserByUsername()
// Получает объект UserDetails (с зашифрованным паролем и ролями)
// Spring Security УЖЕ сходил в БД внутри authenticate()
// Сравнивает введённый пароль с сохранённым с помощью PasswordEncoder.matches()
// Если все ок, то получаем полностью заполненный Authentication
// В переменную authentication попадает объект аутентификации Spring Security (не User)

            // Получаем пользователя с подтвержденной аутентификацией
            User user = (User) authentication.getPrincipal();

            // Генерируем новый JWT токен
            String accessToken = jwtTokenProvider.generateToken(user);
            String refreshToken = refreshTokenService.createRefreshToken(user);


            return AuthResponseDTO.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .expiresAt(LocalDateTime.now().plusSeconds(jwtExpirationInMs / 1000))
                    .build();

        } catch (BadCredentialsException ex) {
            throw new RuntimeException("Invalid username or password");
        }
    }


    @Override
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }



    @Override
    public AuthResponseDTO refresh(String refreshToken) {
        // Проверяем refresh токен и получаем пользователя
        User user = refreshTokenService.validateRefreshToken(refreshToken);

        // Генерируем новый access токен
        String newAccessToken = jwtTokenProvider.generateToken(user);

        // Генерируем новый refresh токен
        String newRefreshToken = refreshTokenService.createRefreshToken(user);

        return AuthResponseDTO.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .expiresAt(LocalDateTime.now().plusSeconds(jwtExpirationInMs / 1000))
                .build();
    }


    @Override
    public void logout(String refreshToken) {
        refreshTokenService.revokeRefreshToken(refreshToken);
    }
}
