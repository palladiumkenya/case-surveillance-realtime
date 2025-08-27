package org.kenyahmis.shared.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.kenyahmis.shared.constants.GlobalConstants;
import org.kenyahmis.shared.dto.EventBase;

public class ValidEventBaseValidator implements ConstraintValidator<ValidEventBase, EventBase<?>> {
    @Override
    public boolean isValid(EventBase value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return GlobalConstants.ROLL_CALL.equals(value.getEventType()) || value.getClient() != null;
    }
}
