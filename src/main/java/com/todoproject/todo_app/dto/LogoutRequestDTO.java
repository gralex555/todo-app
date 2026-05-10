package com.todoproject.todo_app.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LogoutRequestDTO {

    @NotBlank(message = "Refresh token cannot be blank")
    private String refreshToken;
}
