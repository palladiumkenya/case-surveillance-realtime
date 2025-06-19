package org.kenyahmis.shared.dto;

public class APIErrorResponse<T> {
    private T errors;
    private String message;

    public APIErrorResponse() {
    }

    public APIErrorResponse(T errors, String message) {
        this.errors = errors;
        this.message = message;
    }

    public T getErrors() {
        return errors;
    }

    public void setErrors(T errors) {
        this.errors = errors;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
