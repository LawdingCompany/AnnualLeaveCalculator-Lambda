package com.lawding.leavecalc.domain.detail;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdjustedAnnualLeaveDetail implements CalculationDetail {

    // (기본 + 가산) * PWR
    private final int baseAnnualLeave;
    private final int additionalLeave;
    private final int prescribedWorkingDays;
    private final int excludedWorkingDays;
    private final double prescribeWorkingRatio;
    private final double totalLeaveDays;
}
