package com.lawding.leavecalc.exception;

public enum ErrorCode {
    INVALID_CALCULATION_TYPE("E001", "올바르지 않은 연차 산정 방식 코드입니다."),
    INTERNAL_SERVER_ERROR("E999", "서버 오류가 발생했습니다.");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
