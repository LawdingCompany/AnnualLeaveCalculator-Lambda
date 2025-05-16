package com.lawding.leavecalc.exception;

public enum ErrorCode {
    INVALID_CALCULATION_TYPE("E001", "올바르지 않은 연차 산정 방식 코드입니다."),

    HIRE_DATE_REQUIRED("E100","입사일은 필수 항목입니다."),
    REFERENCE_DATE_REQUIRED("E101","기준일은 필수 항목입니다."),
    FISCAL_YEAR_REQUIRED("E102","회계연도 방식일 경우, 회계연도 시작일은 필수입니다."),

    RDS_ENV_MISSING("E200","RDS 환경변수 설정에 실패했습니다."),
    JDBC_DRIVER_NOT_FOUND("E201","JDBC 드라이버를 찾을 수 없습니다."),
    DATABASE_CONNECTION_FAILED("E202","데이터베이스 연결에 실패했습니다."),
    DATABASE_CONNECTION_TIMEOUT("E203","데이터베이스 연결 시간이 초과되었습니다."),
    IAM_AUTH_REGION_ERROR("E204", "IAM 인증을 위한 리전 설정에 오류가 발생했습니다."),
    IAM_AUTH_CREDENTIALS_ERROR("E205", "IAM 인증을 위한 자격 증명 획득에 실패했습니다."),
    IAM_AUTH_TOKEN_FAILED("E206", "IAM 인증 토큰 생성에 실패했습니다."),
    DATABASE_QUERY_FAILED("E207", "공휴일 조회에 실패했습니다."),

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
