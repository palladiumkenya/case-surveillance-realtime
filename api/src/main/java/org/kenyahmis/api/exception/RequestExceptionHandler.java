package org.kenyahmis.api.exception;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import org.kenyahmis.shared.dto.APIErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class RequestExceptionHandler extends ResponseEntityExceptionHandler {
    private final Logger LOG = LoggerFactory.getLogger(RequestExceptionHandler.class);

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return new ResponseEntity<>(new APIErrorResponse<>(errors, "Request validation error"),
                HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> error = new HashMap<>();
        error.put("unreadableField", ex.getMessage());
        return new ResponseEntity<>(new APIErrorResponse<>(error, "Invalid Request"), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RequestValidationException.class)
    protected ResponseEntity<APIErrorResponse<?>> handleException(RequestValidationException ex) {
        for (Map.Entry<String, String> entry : ex.getErrors().entrySet()) {
            LOG.error(entry.getKey() + " => " + entry.getValue() + " => Sites " + ex.mflCodes);
        }
        return new ResponseEntity<>(new APIErrorResponse<>(ex.getErrors(), "Invalid Request"), HttpStatus.BAD_REQUEST) ;
    }

    @ExceptionHandler(UnrecognizedPropertyException.class)
    protected ResponseEntity<APIErrorResponse<?>> handleException(UnrecognizedPropertyException ex) {
        LOG.info("Handling validation error");
        return new ResponseEntity<>(new APIErrorResponse<>(ex.getMessage(), "Invalid Request"), HttpStatus.BAD_REQUEST) ;
    }
}
