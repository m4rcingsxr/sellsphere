package com.sellsphere.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.function.BooleanSupplier;

/**
 * Helper class for handling validation logic for various fields such as password,
 * file uploads, and custom validation through boolean checks. This class leverages
 * Spring's {@link BindingResult} to record validation errors and associate them with
 * specific fields in the form.
 *
 * <p>Usage:
 * <ul>
 *     <li>Create an instance by passing a {@link BindingResult} and an error code.</li>
 *     <li>Call the appropriate validation method (e.g., validatePassword, validateMultipartFile).</li>
 *     <li>Check for validation errors using {@link #validate()} before proceeding with business logic.</li>
 * </ul>
 *
 * <p>This helper allows flexible validation logic to be encapsulated and reused across different parts
 * of the application.
 */
@RequiredArgsConstructor
@Slf4j
public class ValidationHelper {

    private final BindingResult bindingResult;
    private final String errorCode;

    private static final String PASSWORD_PATTERN = "^(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d]{8,72}$";

    /**
     * Validates a MultipartFile.
     *
     * @param file the file to validate
     * @param entityId the ID of the entity
     * @param fieldName the field name
     * @param message the validation message
     */
    public void validateMultipartFile(MultipartFile file, Integer entityId, String fieldName, String message) {
        log.info("Validating file for field '{}', entityId: {}", fieldName, entityId);

        if (entityId == null && (file == null || file.isEmpty())) {
            log.error("File validation failed for field '{}': {}", fieldName, message);
            this.bindingResult.rejectValue(fieldName, this.errorCode, message);
        } else {
            log.info("File validation passed for field '{}'", fieldName);
        }
    }

    /**
     * Validates a password.
     *
     * @param plainPassword the plain text password
     * @param entityId the ID of the entity
     */
    public void validatePassword(String plainPassword, Integer entityId) {
        log.info("Validating password for entityId: {}", entityId);

        if (entityId != null && (plainPassword == null || plainPassword.isEmpty())) {
            log.info("Password validation skipped for existing user with entityId: {}", entityId);
            return;
        }

        String fieldName = "password";

        if (plainPassword == null || plainPassword.isEmpty()) {
            log.error("Password is missing for new user");
            bindingResult.rejectValue(fieldName, errorCode, "Password is required for new users.");
        } else if (plainPassword.length() < 8) {
            log.error("Password validation failed: too short");
            bindingResult.rejectValue(fieldName, errorCode, "Password must be at least 8 characters long.");
        } else if (plainPassword.length() > 64) {
            log.error("Password validation failed: too long");
            bindingResult.rejectValue(fieldName, errorCode, "Password must be no more than 64 characters long.");
        } else if (!plainPassword.matches(PASSWORD_PATTERN)) {
            log.error("Password validation failed: missing required characters");
            bindingResult.rejectValue(fieldName, errorCode, "Password must contain at least one uppercase letter and one digit.");
        } else {
            log.info("Password validation passed");
        }
    }

    /**
     * Validates using a BooleanSupplier.
     *
     * @param isValid the BooleanSupplier that provides the validation result
     * @param fieldName the field name
     * @param message the validation message
     */
    public void validateWithBooleanSupplier(BooleanSupplier isValid, String fieldName, String message) {
        log.info("Validating custom condition for field '{}'", fieldName);

        if (!isValid.getAsBoolean()) {
            log.error("Custom validation failed for field '{}': {}", fieldName, message);
            bindingResult.rejectValue(fieldName, this.errorCode, message);
        } else {
            log.info("Custom validation passed for field '{}'", fieldName);
        }
    }

    /**
     * Checks if there are any validation errors.
     *
     * @return true if there are no validation errors, false otherwise
     */
    public boolean validate() {
        boolean hasErrors = bindingResult.hasErrors();
        if (hasErrors) {
            log.error("Validation failed with errors: {}", bindingResult.getAllErrors());
        } else {
            log.info("Validation passed with no errors");
        }
        return !hasErrors;
    }
}
