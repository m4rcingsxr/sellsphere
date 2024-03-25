package com.sellsphere.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
public class ValidationHelper {

    private final BindingResult bindingResult;
    private final String errorCode;

    private static final String PASSWORD_PATTERN = "^(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d]{8,72}$";


    public void validateMultipartFile(MultipartFile file, Integer entityId, String fieldName,
                                      String message) {
        if (entityId == null && (file == null || file.isEmpty())) {
            this.bindingResult.rejectValue(fieldName, this.errorCode, message);
        }
    }

    public void validatePassword(String plainPassword, Integer entityId) {
        if (entityId != null && (plainPassword == null || plainPassword.isEmpty())) {
            return;
        }

        final String fieldName = "password";

        if (plainPassword == null || plainPassword.isEmpty()) {
            bindingResult.rejectValue(fieldName, errorCode, "Password is required for new users.");
        } else {
            if (plainPassword.length() < 8) {
                bindingResult.rejectValue(fieldName, errorCode,
                                          "Password must be at least 8 characters long."
                );
            } else if (plainPassword.length() > 64) {
                bindingResult.rejectValue(fieldName, errorCode,
                                          "Password must be no more than 64 characters long."
                );
            } else if (!plainPassword.matches(PASSWORD_PATTERN)) {
                bindingResult.rejectValue(fieldName, errorCode,
                                          "Password must contain at least one uppercase letter " +
                                                  "and one digit."
                );
            }
        }
    }

    public boolean validate() {
        return !bindingResult.hasErrors();
    }
}
