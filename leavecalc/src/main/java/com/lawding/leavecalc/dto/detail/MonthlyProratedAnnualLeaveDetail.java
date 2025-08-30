package com.lawding.leavecalc.dto.detail;

import com.lawding.leavecalc.domain.DatePeriod;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class MonthlyProratedAnnualLeaveDetail implements CalculationDetail {
    // 월차 + 15 * PWR
    private final DatePeriod monthlyLeaveAccrualPeriod;
    private final double monthlyLeaveDays;
    private final DatePeriod proratedLeaveAccrualPeriod;
    private final double proratedLeaveDays;
    private final double totalLeaveDays;
}
