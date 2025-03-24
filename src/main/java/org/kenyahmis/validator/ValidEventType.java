package org.kenyahmis.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy = ValidEventTypeValidator.class)
@Target({ TYPE, FIELD, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Documented
public @interface ValidEventType {
    String message() default "Invalid Event type. The type can be: linked_case, new_case, at_risk_pbfw," +
            " prep_linked_at_risk_pbfw, eligible_for_vl, unsuppressed_viral_load";
    Class <?> [] groups() default {};
    Class <? extends Payload> [] payload() default {};
}
