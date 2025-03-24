package org.kenyahmis.exception;

import lombok.Getter;

import java.util.Map;

@Getter
public class RequestValidationException extends RuntimeException{
    Map<String, String> errors;

    public RequestValidationException(Map<String, String> errors) {
        this.errors = errors;
    }

    public RequestValidationException(String message, Map<String, String> errors) {
        super(message);
        this.errors = errors;
    }
}
