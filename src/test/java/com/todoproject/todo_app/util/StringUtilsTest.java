package com.todoproject.todo_app.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class StringUtilsTest {

    @Test
    void isBlankShouldReturnTrueWhenInputIsNull() {
        String input  = null;

        boolean result = StringUtils.isBlank(input);
        assertTrue(result);
    }

    @Test
    void isBlankShouldReturnTrueWhenInputIsEmptyString() {
        String input = "";
        boolean result = StringUtils.isBlank(input);
        assertTrue(result);

    }

    @Test
    void isBlankShouldReturnFalseWhenInputContainsText() {
        String input = "hello";
        boolean result = StringUtils.isBlank(input);
        assertFalse(result);
    }

    @Test
    void capitalizeShouldMakeFirstLetterUppercaseWhenInputIsLowercase() {
        String input = "hello";
        String result = StringUtils.capitalize(input);
        assertEquals("Hello", result);
    }

    @Test
    void capitalizeShouldOnlyChangeFirstLetterWhenInputHasMultipleWords() {
        String input = "hello world";
        String result = StringUtils.capitalize(input);
        assertEquals("Hello world", result);
    }

    @Test
    void capitalizeShouldReturnNullWhenInputIsNull() {
        String input = null;
        String result = StringUtils.capitalize(input);
        assertNull(result);
    }

    @Test
    void capitalizeShouldReturnEmptyStringWhenInputIsEmpty() {
        String input = "";
        String result = StringUtils.capitalize(input);
        assertEquals("", result);
    }

    @Test
    void truncateShouldThrowExceptionWhenTextIsNull() {

        assertThrows(IllegalArgumentException.class, () -> {
            StringUtils.truncate(null, 5);
        });
    }

    @Test
    void truncateShouldThrowExceptionWhenMaxLengthIsNegative() {
        assertThrows(IllegalArgumentException.class, () -> {
            StringUtils.truncate("hello", -1);
        });
    }

    @Test
    void truncateShouldReturnSameTextWhenTextIsShorterThanMaxLength() {
        String input = "hi";
        int inputMaxLength = 10;

        String result = StringUtils.truncate(input, inputMaxLength);
        assertEquals("hi", result);

    }

    @Test
    void truncateShouldTruncateAndAddEllipsisWhenTextIsLongerThanMaxLength() {
        String input = "hello world";       // длина 11
        int maxLength = 5;

        String result = StringUtils.truncate(input, maxLength);
        assertEquals("hello...", result);

    }

    @Test
    void truncateShouldReturnSameTextWhenTextLengthEqualsMaxLength() {
        String input = "hello";
        int maxLength = 5;
        String result = StringUtils.truncate(input, maxLength);
        assertEquals("hello", result);
    }

}
