package org.kenyahmis.shared.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy = ValidPrepStatusValidator.class)
@Target({ TYPE, FIELD, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Documented
public @interface ValidPrepStatus {
    String message() default "Invalid PrEP status. The value can be start, continue, restart, switch, discontinue";
    Class <?> [] groups() default {};
    Class <? extends Payload> [] payload() default {};
}
