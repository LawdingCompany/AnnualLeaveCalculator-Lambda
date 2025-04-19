package com.lawding.leavecalc.exception;

public class AnnualLeaveException extends RuntimeException {

    private final ErrorCode errorCode;

    public AnnualLeaveException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
