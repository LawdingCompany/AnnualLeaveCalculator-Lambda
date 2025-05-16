package com.lawding.leavecalc.exception;

import lombok.Getter;

@Getter
public class AnnualLeaveException extends RuntimeException {

    private final ErrorCode errorCode;

    public AnnualLeaveException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

}
