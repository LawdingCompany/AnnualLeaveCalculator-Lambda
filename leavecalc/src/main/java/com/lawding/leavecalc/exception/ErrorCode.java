package com.lawding.leavecalc.exception;

public enum ErrorCode {
    INVALID_CALCULATION_TYPE("E001", "올바르지 않은 연차 산정 방식 코드입니다."),

    HIRE_DATE_REQUIRED("E100","입사일은 필수 항목입니다."),
    REFERENCE_DATE_REQUIRED("E101","기준일은 필수 항목입니다."),
    FISCAL_YEAR_REQUIRED("E102","회계연도 방식일 경우, 회계연도 시작일은 필수입니다."),

    DATABASE_CONNECTION_FAILED("E201","데이터베이스 연결에 실패했습니다."),
    DATABASE_QUERY_FAILED("E202", "공휴일 조회에 실패했습니다."),

    SERIALIZATION_FAILED("E400", "응답 객체 직렬화에 실패했습니다."),

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
