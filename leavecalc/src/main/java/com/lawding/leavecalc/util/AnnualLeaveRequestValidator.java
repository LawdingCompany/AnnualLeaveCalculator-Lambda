package com.lawding.leavecalc.util;

import com.lawding.leavecalc.dto.AnnualLeaveRequest;
import com.lawding.leavecalc.exception.AnnualLeaveException;
import com.lawding.leavecalc.exception.ErrorCode;

public class AnnualLeaveRequestValidator {

    private AnnualLeaveRequestValidator() {
    }

    public static void validate(AnnualLeaveRequest request) {
        if (request.getHireDate() == null || request.getHireDate().isBlank()) {
            throw new AnnualLeaveException(ErrorCode.HIRE_DATE_REQUIRED);
        }
        if (request.getReferenceDate() == null || request.getReferenceDate().isBlank()) {
            throw new AnnualLeaveException(ErrorCode.REFERENCE_DATE_REQUIRED);
        }
        if (request.getCalculationType() == 2 && (request.getFiscalYear() == null
            || request.getFiscalYear().isBlank())) {
            throw new AnnualLeaveException(ErrorCode.FISCAL_YEAR_REQUIRED);
        }
    }
}
