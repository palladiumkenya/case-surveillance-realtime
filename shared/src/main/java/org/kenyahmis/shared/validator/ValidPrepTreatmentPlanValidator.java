package org.kenyahmis.shared.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class ValidPrepTreatmentPlanValidator implements ConstraintValidator<ValidPrepTreatmentPlan, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        List<String> response = List.of("START", "CONTINUE", "RESTART", "SWITCH", "DISCONTINUE");
        return response.contains(value.toUpperCase());
    }
}
