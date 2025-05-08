package io.vladprotchenko.authservice.dto.validation;


import io.vladprotchenko.ensstartercore.exception.custom.InternalServiceException;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Field;

public class PasswordMatchValidator implements ConstraintValidator<PasswordMatch, Object> {

    private String passwordField;
    private String confirmPasswordField;
    private String message;

    @Override
    public void initialize(PasswordMatch constraintAnnotation) {
        this.passwordField = constraintAnnotation.passwordField();
        this.confirmPasswordField = constraintAnnotation.passwordConfirmation();
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        try {
            Field password = value.getClass().getDeclaredField(this.passwordField);
            Field confirmPassword = value.getClass().getDeclaredField(this.confirmPasswordField);

            password.setAccessible(true);
            confirmPassword.setAccessible(true);

            Object passwordValue = password.get(value);
            Object confirmPasswordValue = confirmPassword.get(value);

            if (passwordValue == null || confirmPasswordValue == null) {
                return false;
            }

            boolean isValid = passwordValue.equals(confirmPasswordValue);

            if (!isValid) {
                context.disableDefaultConstraintViolation();
                context
                    .buildConstraintViolationWithTemplate(message)
                    .addPropertyNode(
                        this.confirmPasswordField)
                    .addConstraintViolation();
            }

            return isValid;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new InternalServiceException(String.format("Error accessing password fields. Details: %s", e));
        }
    }
}
