package vn.com.huylq.libraryeventsproducer.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class LibraryEventControllerAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleRequestBody(MethodArgumentNotValidException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        String error = fieldErrors.stream()
                                  .map(fieldError -> fieldError.getField() + " - " + fieldError.getDefaultMessage())
                                  .sorted()
                                  .collect(Collectors.joining(", "));

        log.info("errorMessage: {}", error);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}
