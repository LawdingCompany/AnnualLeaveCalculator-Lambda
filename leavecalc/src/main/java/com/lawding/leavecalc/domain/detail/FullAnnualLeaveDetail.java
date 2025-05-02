package com.lawding.leavecalc.domain.detail;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FullAnnualLeaveDetail implements CalculationDetail {
    // 기본 + 가산
    private final int baseAnnualLeave;
    private final int additionalLeave;
    private final double totalLeaveDays;
}
