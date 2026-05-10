package com.todoproject.todo_app.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenRequestDTO {
    @NotBlank(message = "Refresh token cannot be blank")
    private String refreshToken;
}
