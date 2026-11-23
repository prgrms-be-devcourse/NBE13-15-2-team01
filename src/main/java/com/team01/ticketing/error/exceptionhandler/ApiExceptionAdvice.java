package com.team01.ticketing.error.exceptionhandler;

import com.team01.ticketing.error.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ApiExceptionAdvice {

    @ExceptionHandler({
            VALIDATION_ERROR.class,
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            HandlerMethodValidationException.class
    })
    public ResponseEntity<ApiError> handlerValidationError() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiError("VALIDATION_ERROR", "입력값을 확인해 주세요."));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handlerBodyValidationError(MethodArgumentNotValidException e) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            String name = error.getField().split("[\\[.]")[0];
            fieldErrors.putIfAbsent(name, error.getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiError("VALIDATION_ERROR", "입력값을 확인해 주세요.", fieldErrors));
    }

    @ExceptionHandler(LOGIN_FAILED.class)
    public ResponseEntity<ApiError> handlerUnauthorized(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiError(e.getClass().getSimpleName(), e.getMessage()));
    }

    @ExceptionHandler(NOT_RESERVATION_OWNER.class)
    public ResponseEntity<ApiError> handlerForbidden(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiError(e.getClass().getSimpleName(), e.getMessage()));
    }

    @ExceptionHandler({
            MEMBER_NOT_FOUND.class,
            PERFORMANCE_NOT_FOUND.class,
            SCHEDULE_NOT_FOUND.class,
            SEAT_NOT_FOUND.class,
            RESERVATION_NOT_FOUND.class
    })
    public ResponseEntity<ApiError> handlerNotFound(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError(e.getClass().getSimpleName(), e.getMessage()));
    }

    @ExceptionHandler({
            EMAIL_DUPLICATED.class,
            SCHEDULE_NOT_BOOKABLE.class,
            SEAT_ALREADY_RESERVED.class,
            RESERVATION_ALREADY_CANCELED.class,
            CANCEL_NOT_ALLOWED.class,
            TICKET_LIMIT_EXCEEDED.class
    })
    public ResponseEntity<ApiError> handlerConflict(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError(e.getClass().getSimpleName(), e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handlerUnexpected(Exception e) {
        log.error("Unexpected error", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError("INTERNAL_ERROR", "서버 오류가 발생했습니다."));
    }
}
