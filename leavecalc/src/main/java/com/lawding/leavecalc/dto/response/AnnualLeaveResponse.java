package com.lawding.leavecalc.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lawding.leavecalc.dto.AnnualLeaveResult;
import com.lawding.leavecalc.domain.AnnualLeaveResultType;
import com.lawding.leavecalc.domain.CalculationType;
import com.lawding.leavecalc.dto.detail.CalculationDetail;
import java.time.LocalDate;
import java.time.MonthDay;

public record AnnualLeaveResponse(
    CalculationType calculationType,
    AnnualLeaveResultType annualLeaveResultType,

    @JsonFormat(pattern = "MM-dd")
    MonthDay fiscalYear,

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate hireDate,

    @JsonFormat(pattern = "yyyy-MM-dd")
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