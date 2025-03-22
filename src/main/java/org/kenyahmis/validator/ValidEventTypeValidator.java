package org.kenyahmis.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class ValidEventTypeValidator implements ConstraintValidator<ValidEventType, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        List<String> eventTypes = List.of("linked_case", "new_case");
        return eventTypes.contains(value);
    }
}
