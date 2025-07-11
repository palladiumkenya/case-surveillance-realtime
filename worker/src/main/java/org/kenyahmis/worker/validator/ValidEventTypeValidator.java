package org.kenyahmis.worker.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

import static org.kenyahmis.shared.constants.GlobalConstants.*;

public class ValidEventTypeValidator implements ConstraintValidator<ValidEventType, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        List<String> eventTypes = List.of(LINKED_EVENT_TYPE, NEW_EVENT_TYPE, AT_RISK_PBFW, PREP_LINKED_AT_RISK_PBFW,
                ELIGIBLE_FOR_VL, UNSUPPRESSED_VIRAL_LOAD, HEI_WITHOUT_PCR, HEI_WITHOUT_FINAL_OUTCOME, HEI_AT_6_TO_8_WEEKS,
                HEI_AT_24_WEEKS);
        return eventTypes.contains(value);
    }
}
