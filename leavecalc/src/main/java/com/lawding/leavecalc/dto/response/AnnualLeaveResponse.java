package com.lawding.leavecalc.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lawding.leavecalc.domain.flow.detail.CalculationDetail;
import com.lawding.leavecalc.dto.request.NonWorkingPeriodDto;
import java.util.List;

/**
 * 최종 API 응답 DTO
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record AnnualLeaveResponse(
    String calculationId,
    String calculationType,
    String fiscalYear,
    String hireDate,
    String referenceDate,
    List<NonWorkingPeriodDto> nonWorkingPeriod,
    List<String> companyHolidays,
    String leaveType,
    CalculationDetail calculationDetail,
    List<String> explanations,
    List<String> nonWorkingExplanations
) {
    public static AnnualLeaveResponse of(AnnualLeaveResult result, String calculationId) {
        return new AnnualLeaveResponse(
            calculationId,
            result.calculationType(),
            result.fiscalYear(),
            result.hireDate(),
            result.referenceDate(),
            result.nonWorkingPeriod(),
            result.companyHolidays(),
            result.leaveType(),
            result.calculationDetail(),
            result.explanations(),
            result.nonWorkingExplanations()
        );
    }
}
