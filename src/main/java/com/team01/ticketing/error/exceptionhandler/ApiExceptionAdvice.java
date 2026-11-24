package com.team01.ticketing.error.exceptionhandler;

import com.team01.ticketing.error.BusinessException;
import com.team01.ticketing.error.ErrorCode;
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

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handlerBusiness(BusinessException e) {
        ErrorCode code = e.getErrorCode();
        return ResponseEntity.status(code.getStatus())
                .body(new ApiError(code.name(), code.getMessage()));
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            HandlerMethodValidationException.class
    })
    //잘못된 요청 예외 4개를 한꺼번에 400으로 바꿔 주는 핸들러
    public ResponseEntity<ApiError> handlerValidationError() {
        ErrorCode code = ErrorCode.VALIDATION_ERROR;
        return ResponseEntity.status(code.getStatus())
                .body(new ApiError(code.name(), code.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    // DTO로 받을 때 건 검증이 실패했을 때 동작하는 핸들러
    public ResponseEntity<ApiError> handlerBodyValidationError(MethodArgumentNotValidException e) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            String name = error.getField().split("[\\[.]")[0];
            fieldErrors.putIfAbsent(name, error.getDefaultMessage());
        }
        ErrorCode code = ErrorCode.VALIDATION_ERROR;
        return ResponseEntity.status(code.getStatus())
                .body(new ApiError(code.name(), code.getMessage(), fieldErrors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handlerUnexpected(Exception e) {
        log.error("Unexpected error", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError("INTERNAL_ERROR", "서버 오류가 발생했습니다."));
    }
}
