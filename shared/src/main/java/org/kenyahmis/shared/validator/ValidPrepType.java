package org.kenyahmis.shared.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy = ValidPrepTypeValidator.class)
@Target({ TYPE, FIELD, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Documented
public @interface ValidPrepType {
    String message() default "Invalid PrEP type. The value can be ORAL, CAB-LA, DAPIVIRINE RING, LENACAPAVIR";
    Class <?> [] groups() default {};
    Class <? extends Payload> [] payload() default {};
}
