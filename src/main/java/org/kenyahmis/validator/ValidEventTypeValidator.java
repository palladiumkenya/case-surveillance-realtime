package org.kenyahmis.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import static org.kenyahmis.constants.GlobalConstants.*;

import java.util.List;

public class ValidEventTypeValidator implements ConstraintValidator<ValidEventType, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        List<String> eventTypes = List.of(LINKED_EVENT_TYPE, NEW_EVENT_TYPE, AT_RISK_PBFW, PREP_LINKED_AT_RISK_PBFW,
                ELIGIBLE_FOR_VL, UNSUPPRESSED_VIRAL_LOAD, HEI_WITHOUT_PCR, HEI_WITHOUT_FINAL_OUTCOME);
        return eventTypes.contains(value);
    }
}
