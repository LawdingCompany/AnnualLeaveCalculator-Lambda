package com.lawding.leavecalc.domain;

import com.lawding.leavecalc.exception.AnnualLeaveException;
import com.lawding.leavecalc.exception.ErrorCode;
import java.util.Arrays;

/**
 * 연차 산정 방식 입사일(1), 회계연도(2)
 */
public enum CalculationType {
    HIRE_DATE(1), FISCAL_YEAR(2);

    private final int code;

    CalculationType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static CalculationType fromCode(int code) {
        return Arrays.stream(values())
            .filter(type -> type.code == code)
            .findFirst()
            .orElseThrow(
                () -> new AnnualLeaveException(ErrorCode.INVALID_CALCULATION_TYPE));
    }
}
