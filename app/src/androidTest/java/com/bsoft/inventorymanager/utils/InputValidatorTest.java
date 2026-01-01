package com.bsoft.inventorymanager.utils;

import static com.google.common.truth.Truth.assertThat;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class InputValidatorTest {

    @Test
    public void isValidEmail_validatesCorrectly() {
        assertThat(InputValidator.isValidEmail("test@example.com")).isTrue();
        assertThat(InputValidator.isValidEmail("invalid")).isFalse();
        assertThat(InputValidator.isValidEmail("")).isFalse();
    }

    @Test
    public void isValidPhone_validatesCorrectly() {
        assertThat(InputValidator.isValidPhone("1234567890")).isTrue();
        assertThat(InputValidator.isValidPhone("+1234567890")).isTrue();
        assertThat(InputValidator.isValidPhone("abc")).isFalse();
    }

    @Test
    public void isValidName_validatesCorrectly() {
        assertThat(InputValidator.isValidName("John Doe")).isTrue();
        assertThat(InputValidator.isValidName("O'Connor")).isTrue();
        assertThat(InputValidator.isValidName("Mary-Jane")).isTrue();

        // Too short
        assertThat(InputValidator.isValidName("A")).isFalse();
        // Special chars not allowed
        assertThat(InputValidator.isValidName("John@Doe")).isFalse();
    }

    @Test
    public void sanitizeInput_removesDangerousCharacters() {
        String input = "<script>alert('xss')</script>";
        // sanitizeInput removes < > ' " & ;
        // So <script> becomes script
        // alert('xss') becomes alert(xss)
        // </script> becomes /script
        // Expected: scriptalert(xss)/script

        // Let's verify exact behavior from code: input.replaceAll("[<>'\"&;]", "")
        String expected = "scriptalert(xss)/script";
        assertThat(InputValidator.sanitizeInput(input)).isEqualTo(expected);

        assertThat(InputValidator.sanitizeInput("Hello & Welcome")).isEqualTo("Hello  Welcome");
    }

    @Test
    public void validateAndSanitizePrice_parsesCorrectly() {
        assertThat(InputValidator.validateAndSanitizePrice("10.50")).isEqualTo(10.50);
        assertThat(InputValidator.validateAndSanitizePrice("-10")).isEqualTo(-1.0); // Invalid because pattern expects
                                                                                    // digit+
        assertThat(InputValidator.validateAndSanitizePrice("abc")).isEqualTo(-1.0);
    }
}
