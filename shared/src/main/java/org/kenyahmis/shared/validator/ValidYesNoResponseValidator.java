package org.kenyahmis.shared.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class ValidYesNoResponseValidator implements ConstraintValidator<ValidYesNoResponse, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        List<String> response = List.of("YES", "NO");
        return response.contains(value.toUpperCase());
    }
}
