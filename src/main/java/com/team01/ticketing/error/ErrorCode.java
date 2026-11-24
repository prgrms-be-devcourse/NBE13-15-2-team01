package com.team01.ticketing.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "입력값을 확인해 주세요."),
    INVALID_PERFORMANCE_DATE_RANGE(HttpStatus.BAD_REQUEST, "종료일은 시작일보다 빠를 수 없습니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    NOT_RESERVATION_OWNER(HttpStatus.FORBIDDEN, "본인의 예매가 아닙니다."),

    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."),
    PERFORMANCE_NOT_FOUND(HttpStatus.NOT_FOUND, "공연을 찾을 수 없습니다."),
    SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "회차를 찾을 수 없습니다."),
    SEAT_NOT_FOUND(HttpStatus.NOT_FOUND, "좌석을 찾을 수 없습니다."),
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "예매를 찾을 수 없습니다."),

    EMAIL_DUPLICATED(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    SCHEDULE_NOT_BOOKABLE(HttpStatus.CONFLICT, "예매할 수 없는 회차입니다."),
    SEAT_ALREADY_RESERVED(HttpStatus.CONFLICT, "이미 예매된 좌석입니다."),
    RESERVATION_ALREADY_CANCELED(HttpStatus.CONFLICT, "이미 취소된 예매입니다."),
    CANCEL_NOT_ALLOWED(HttpStatus.CONFLICT, "취소할 수 없는 시간입니다."),
    TICKET_LIMIT_EXCEEDED(HttpStatus.CONFLICT, "한 회차당 최대 3매까지 예매할 수 있습니다.");

    private final HttpStatus status;
    private final String message;
}
