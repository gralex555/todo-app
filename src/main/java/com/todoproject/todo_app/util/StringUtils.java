package com.todoproject.todo_app.util;

public class StringUtils {

    public static boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }

    public static String capitalize(String text) {  // возвращает строку с заглавной первой буквой
        if (isBlank(text)) {
            return text;
        }
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    // Обрезает строку до maxLength символов и добавляет "...":
    public static String truncate(String text, int maxLength) {
        if (text == null) {
            throw new IllegalArgumentException("Text cannot be null");
        }
        if (maxLength < 0) {
            throw new IllegalArgumentException("Max length cannot be negative");
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }
}
