package org.kenyahmis.api.exception;

import lombok.Getter;

import java.util.Map;
import java.util.Set;

@Getter
public class RequestValidationException extends RuntimeException{
    Map<String, String> errors;
    Set<String> mflCodes;

    public RequestValidationException(Map<String, String> errors, Set<String> mflCodes) {
        this.errors = errors;
        this.mflCodes = mflCodes;
    }

    public RequestValidationException(String message, Map<String, String> errors) {
        super(message);
        this.errors = errors;
    }
}
