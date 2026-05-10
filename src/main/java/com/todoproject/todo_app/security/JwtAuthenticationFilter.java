package com.todoproject.todo_app.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;   // это стандартный интерфейс Spring Security.
  //  задача — по имени пользователя (username) загрузить полную информацию о пользователе (роли, пароль и т.д.).

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
                                   UserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. Извлекаем токен из заголовка Authorization
        String token = getJWTFromRequest(request);

        // 2. Проверяем, есть ли токен и валиден ли он
        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {  // StringUtils.hasText(token) проверяет строку на наличие текста по параметрам:
                     // что строка не пустая, что не состоит из пробелов, что строка не null

            // 3. Извлекаем username из токена
            String username = jwtTokenProvider.getUsernameFromJWT(token);

            // 4. по имени пользователя получаем данные пользователя: username, зашифрованный пароль, список ролей
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // 5. Создаём объект аутентификации
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()  // список прав (ролей) пользователя
                    );

            // 6. Добавляем допольнительные детали запроса
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // 7. Устанавливаем пользователя в SecurityContext, чтобы потом можно было использ данные об этом пользователе далее в Контроллере и сервисе
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

        // 8. Продолжаем цепочку фильтров. Обязательная строка. Передаёт запрос дальше по цепочке фильтров и в контроллер
        filterChain.doFilter(request, response);
    }

     // Извлекает JWT токен из заголовка "Authorization: Bearer <token>"
    private String getJWTFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // убираем "Bearer "
        }

        return null;
    }
}
