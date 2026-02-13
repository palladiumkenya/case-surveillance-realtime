package org.kenyahmis.shared.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class ValidPrepDosingStrategyValidator implements ConstraintValidator<ValidPrepDosingStrategy, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        List<String> response = List.of("EVENT DRIVEN", "DAILY ORAL PREP", "LONG ACTING PREP");
        return response.contains(value.toUpperCase());
    }
}
