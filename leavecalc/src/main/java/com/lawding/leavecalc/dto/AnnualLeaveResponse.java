package com.lawding.leavecalc.dto;

import com.lawding.leavecalc.domain.AnnualLeaveResult;
import com.lawding.leavecalc.domain.AnnualLeaveResultType;
import com.lawding.leavecalc.domain.CalculationType;
import com.lawding.leavecalc.domain.detail.CalculationDetail;
import java.time.LocalDate;
import java.time.MonthDay;

public record AnnualLeaveResponse(
    CalculationType calculationType,
    AnnualLeaveResultType annualLeaveResultType,
    MonthDay fiscalYear,
    LocalDate hireDate,
    LocalDate referenceDate,
    CalculationDetail calculationDetail,
    String explanation
) {
    public static AnnualLeaveResponse of(AnnualLeaveResult result) {
        return new AnnualLeaveResponse(
            result.getCalculationType(),
            result.getAnnualLeaveResultType(),
            result.getFiscalYear(),
            result.getHireDate(),
            result.getReferenceDate(),
            result.getCalculationDetail(),
            result.getExplanation()
        );
    }
}
