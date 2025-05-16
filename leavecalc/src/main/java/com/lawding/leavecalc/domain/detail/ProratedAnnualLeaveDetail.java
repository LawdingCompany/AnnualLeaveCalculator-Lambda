package com.lawding.leavecalc.domain.detail;

import com.lawding.leavecalc.domain.DatePeriod;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProratedAnnualLeaveDetail implements CalculationDetail {

    // 15 * PWR
    private final DatePeriod proratedLeaveAccrualPeriod;
    private final double totalLeaveDays;
}
