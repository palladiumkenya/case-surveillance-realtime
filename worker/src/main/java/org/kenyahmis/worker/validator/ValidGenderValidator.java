package org.kenyahmis.worker.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class ValidGenderValidator implements ConstraintValidator<ValidGender, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        List<String>  genderList = List.of("MALE", "FEMALE", "male", "female", "Male", "Female");
        return genderList.contains(value);
    }
}
