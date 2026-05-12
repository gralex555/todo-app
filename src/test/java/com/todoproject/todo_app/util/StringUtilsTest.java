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

}
