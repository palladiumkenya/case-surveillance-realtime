package org.kenyahmis.shared.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.kenyahmis.shared.constants.GlobalConstants;

import java.util.List;

public class ValidEventTypeValidator implements ConstraintValidator<ValidEventType, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        List<String> eventTypes = List.of(GlobalConstants.LINKED_EVENT_TYPE, GlobalConstants.NEW_EVENT_TYPE, GlobalConstants.AT_RISK_PBFW, GlobalConstants.PREP_LINKED_AT_RISK_PBFW,
                GlobalConstants.ELIGIBLE_FOR_VL, GlobalConstants.UNSUPPRESSED_VIRAL_LOAD, GlobalConstants.HEI_WITHOUT_PCR, GlobalConstants.HEI_WITHOUT_FINAL_OUTCOME, GlobalConstants.HEI_AT_6_TO_8_WEEKS,
                GlobalConstants.HEI_AT_24_WEEKS,GlobalConstants.PREP_UPTAKE, GlobalConstants.ROLL_CALL);
        return eventTypes.contains(value);
    }
}
