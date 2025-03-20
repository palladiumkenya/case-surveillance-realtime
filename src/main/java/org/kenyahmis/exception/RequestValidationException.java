package org.kenyahmis.exception;

import java.util.Map;

public class RequestValidationException extends Exception{
    Map<String, String> errors;

    public Map<String, String> getErrors() {
        return errors;
    }

    public RequestValidationException(Map<String, String> errors) {
        this.errors = errors;
    }

    public RequestValidationException(String message, Map<String, String> errors) {
        super(message);
        this.errors = errors;
    }
}
