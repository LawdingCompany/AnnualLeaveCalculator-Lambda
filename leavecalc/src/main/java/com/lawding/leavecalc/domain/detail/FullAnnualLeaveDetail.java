package com.lawding.leavecalc.domain.detail;

import com.lawding.leavecalc.domain.DatePeriod;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FullAnnualLeaveDetail implements CalculationDetail {
    // 기본 + 가산
    private final DatePeriod accrualPeriod;
    private final int baseAnnualLeave;
    private final int serviceYears;
    private final int additionalLeave;
    private final double totalLeaveDays;

}
