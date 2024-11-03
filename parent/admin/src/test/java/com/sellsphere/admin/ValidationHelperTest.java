package com.sellsphere.admin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.function.BooleanSupplier;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

class ValidationHelperTest {

    private BindingResult bindingResult;
    private ValidationHelper validationHelper;
    private static final String ERROR_CODE = "error.code";

    @BeforeEach
    void setUp() {
        bindingResult = mock(BindingResult.class);
        validationHelper = new ValidationHelper(bindingResult, ERROR_CODE);
    }

    @ParameterizedTest
    @CsvSource({
            ", , file, File is required", // Null file and entityId
            "'', , file, File is required"    // Empty file and entityId
    })
    void givenInvalidFileOrEntityId_whenValidateMultipartFile_thenShouldRejectValue(String fileContent, Integer entityId, String fieldName, String message) {
        // Given
        MultipartFile file = fileContent == null ? null : mock(MultipartFile.class);
        if (file != null) {
            given(file.isEmpty()).willReturn(fileContent.isEmpty());
        }

        // When
        validationHelper.validateMultipartFile(file, entityId, fieldName, message);

        // Then
        then(bindingResult).should().rejectValue(fieldName, ERROR_CODE, message);
    }

    @ParameterizedTest
    @CsvSource({
            ", Password is required for new users.",
            "Short1, Password must be at least 8 characters long.",
    })
    void givenInvalidPassword_whenValidatePassword_thenShouldRejectValue(String password, String expectedMessage) {
        // When
        validationHelper.validatePassword(password, null);

        // Then
        then(bindingResult).should().rejectValue("password", ERROR_CODE, expectedMessage);
    }

    @Test
    void givenValidFileAndEntityId_whenValidateMultipartFile_thenShouldNotRejectValue() {
        // Given
        MultipartFile file = mock(MultipartFile.class);
        given(file.isEmpty()).willReturn(false);
        Integer entityId = 1;
        String fieldName = "file";

        // When
        validationHelper.validateMultipartFile(file, entityId, fieldName, "");

        // Then
        then(bindingResult).shouldHaveNoInteractions();
    }

    @Test
    void givenPasswordWithoutUppercaseOrDigit_whenValidatePassword_thenShouldRejectValue() {
        // Given
        String password = "lowercasepassword";

        // When
        validationHelper.validatePassword(password, null);

        // Then
        then(bindingResult).should().rejectValue("password", ERROR_CODE, "Password must contain at least one uppercase letter and one digit.");
    }

    @Test
    void givenValidPassword_whenValidatePassword_thenShouldNotRejectValue() {
        // Given
        String password = "ValidPass1";

        // When
        validationHelper.validatePassword(password, null);

        // Then
        then(bindingResult).shouldHaveNoInteractions();
    }

    @Test
    void givenExistingUserAndNullPassword_whenValidatePassword_thenShouldNotRejectValue() {
        // Given
        String password = null;
        Integer entityId = 1;

        // When
        validationHelper.validatePassword(password, entityId);

        // Then
        then(bindingResult).shouldHaveNoInteractions();
    }

    @Test
    void givenInvalidBooleanSupplier_whenValidateWithBooleanSupplier_thenShouldRejectValue() {
        // Given
        BooleanSupplier isValid = mock(BooleanSupplier.class);
        given(isValid.getAsBoolean()).willReturn(false);

        String fieldName = "customField";
        String message = "Custom validation failed";

        // When
        validationHelper.validateWithBooleanSupplier(isValid, fieldName, message);

        // Then
        then(bindingResult).should().rejectValue(fieldName, ERROR_CODE, message);
    }

    @Test
    void givenValidBooleanSupplier_whenValidateWithBooleanSupplier_thenShouldNotRejectValue() {
        // Given
        BooleanSupplier isValid = mock(BooleanSupplier.class);
        given(isValid.getAsBoolean()).willReturn(true);

        String fieldName = "customField";

        // When
        validationHelper.validateWithBooleanSupplier(isValid, fieldName, "");

        // Then
        then(bindingResult).shouldHaveNoInteractions();
    }
}
