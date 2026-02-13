package org.kenyahmis.shared.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy = ValidPrepRegimenValidator.class)
@Target({ TYPE, FIELD, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Documented
public @interface ValidPrepRegimen {
    String message() default "Invalid PrEP regimen. The value can be TDF/FTC, TDF/3TC, TAF/FTC";
    Class <?> [] groups() default {};
    Class <? extends Payload> [] payload() default {};
}
