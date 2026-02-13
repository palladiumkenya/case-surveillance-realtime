package org.kenyahmis.shared.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class ValidPrepTypeValidator implements ConstraintValidator<ValidPrepType, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        List<String> response = List.of("ORAL", "CAB-LA", "DAPIVIRINE RING", "LENACAPAVIR");
        return response.contains(value.toUpperCase());
    }
}
