package org.kenyahmis.shared.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ValidTimestampValidator implements ConstraintValidator<ValidTimestamp, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try {
            LocalDateTime.parse(value, DATE_TIME_FORMATTER);
            return true;
        } catch (DateTimeException e) {
            try {
                LocalDate.parse(value, DATE_FORMATTER);
                return true;
            } catch (DateTimeParseException ex) {
                return false;
            }
        }
    }
}
