package com.todoproject.todo_app.exception;

public class UserAlreadyExistsException extends RuntimeException{
    public UserAlreadyExistsException(String message) {
        super(message);
    }

    // конструктор с причиной (cause), если понадобится
    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
