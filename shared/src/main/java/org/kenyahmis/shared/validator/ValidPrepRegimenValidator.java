package org.kenyahmis.shared.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class ValidPrepRegimenValidator implements ConstraintValidator<ValidPrepRegimen, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        List<String> response = List.of("TDF/FTC", "TDF/3TC", "TAF/FTC");
        return response.contains(value.toUpperCase());
    }
}
