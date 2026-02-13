package org.kenyahmis.shared.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy = ValidPrepDosingStrategyValidator.class)
@Target({ TYPE, FIELD, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Documented
public @interface ValidPrepDosingStrategy {
    String message() default "Invalid PrEP dosing strategy. The value can be EVENT DRIVEN, DAILY ORAL PREP,LONG ACTING PREP";
    Class <?> [] groups() default {};
    Class <? extends Payload> [] payload() default {};
}
