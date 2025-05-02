package com.lawding.leavecalc.domain.detail;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProratedAnnualLeaveDetail implements CalculationDetail {
    // 15 * PWR
    private final double totalLeaveDays;
}
