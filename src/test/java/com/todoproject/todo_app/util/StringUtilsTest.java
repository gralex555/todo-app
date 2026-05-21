package com.todoproject.todo_app.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Тесты класса StringUtils")
public class StringUtilsTest {

    @Nested
    @DisplayName("Метод isBlank")
    class isBlankTests {

        @ParameterizedTest
        @DisplayName("возвращает true для null, пустой строки и пробелов")
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t"})
        void shouldReturnTrueForBlankInput(String input) {
            assertThat(StringUtils.isBlank(input)).isTrue();
        }

        @ParameterizedTest
        @DisplayName("возвращает false для непустых строк")
        @ValueSource(strings = {"hello", "a", "Hello World", " text "})
        void shouldReturnFalseForNonBlankInput(String input) {
            assertThat(StringUtils.isBlank(input)).isFalse();
        }
    }

    @Nested
    @DisplayName("Метод capitalize")
    class CapitalizeTests {

        @ParameterizedTest
        @DisplayName("возвращает тот же вход для null и пустой строки")
        @NullAndEmptySource
        void capitalizeShouldReturnSameValueWhenInputIsNullOrEmpty(String input) {

            assertThat(StringUtils.capitalize(input)).isEqualTo(input);
        }

        @ParameterizedTest
        @DisplayName("делает первую букву заглавной")
        @CsvSource({
                "hello, Hello",
                "hello world, Hello world"
        })
        void capitalizeShouldWorkWithFirstLetter(String input, String expected) {
            assertThat(StringUtils.capitalize(input)).isEqualTo(expected);

        }
    }

    @Nested
    @DisplayName("Метод truncate")
    class TruncateTests {

        @Test
        @DisplayName("бросает исключение при null входе")
        void truncateShouldThrowExceptionWhenTextIsNull() {

//            assertThrows(IllegalArgumentException.class, () -> {
//                StringUtils.truncate(null, 5);
//            });

            assertThatThrownBy(() -> StringUtils.truncate(null, 5))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("бросает исключение при отрицательном maxLength")
        void truncateShouldThrowExceptionWhenMaxLengthIsNegative() {
//            assertThrows(IllegalArgumentException.class, () -> {
//                StringUtils.truncate("hello", -1);
//            });
            assertThatThrownBy(() -> StringUtils.truncate("hello", -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @DisplayName("возвращает корректно обрезанный или оригинальный текст")
        @CsvSource({
                "hi, 10, hi",
                "hello world, 5, hello...",
                "hello, 5, hello"
        })
        void truncateShouldReturnExpectedText(String input, int maxLength, String expected) {
           // assertEquals(expected, StringUtils.truncate(input, maxLength));
            assertThat(StringUtils.truncate(input, maxLength)).isEqualTo(expected);
        }
    }

}
