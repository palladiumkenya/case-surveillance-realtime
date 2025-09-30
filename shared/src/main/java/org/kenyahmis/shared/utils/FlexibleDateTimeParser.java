package org.kenyahmis.shared.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class FlexibleDateTimeParser {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static LocalDateTime parse(String input) {
        try {
            return LocalDateTime.parse(input, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            try {
                LocalDate date = LocalDate.parse(input, DATE_FORMATTER);
                return date.atStartOfDay();
            } catch (DateTimeParseException ex) {
                throw new IllegalArgumentException("Unsupported date format: " + input, ex);
            }
        }
    }
}
