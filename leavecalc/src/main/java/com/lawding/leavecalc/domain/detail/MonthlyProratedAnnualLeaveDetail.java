package com.lawding.leavecalc.domain.detail;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MonthlyProratedAnnualLeaveDetail implements CalculationDetail {
    // 월차 + 15 * PWR
    private final double totalLeaveDays;
}
