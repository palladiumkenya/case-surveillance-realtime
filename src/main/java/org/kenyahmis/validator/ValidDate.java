package org.kenyahmis.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy = ValidTimestampValidator.class)
@Target({ TYPE, FIELD, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Documented
public @interface ValidDate {
    String message() default "Invalid Date. Timestamp should confirm to yyyy-MM-dd";
    Class <?> [] groups() default {};
    Class <? extends Payload> [] payload() default {};
}
