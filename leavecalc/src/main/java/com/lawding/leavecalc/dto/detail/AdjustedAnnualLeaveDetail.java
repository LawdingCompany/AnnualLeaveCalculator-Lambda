package com.lawding.leavecalc.dto.detail;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class AdjustedAnnualLeaveDetail implements CalculationDetail {

    // (기본 + 가산) * PWR
    private final int baseAnnualLeave;
    private final int serviceYears;
    private final int additionalLeave;
    private final int prescribedWorkingDays;
    private final int excludedWorkingDays;
    private final double prescribeWorkingRatio;
    private final double totalLeaveDays;
}
