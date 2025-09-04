package com.lawding.leavecalc.domain.flow.detail;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@ToString
public abstract class CalculationDetail {
    private final double totalLeaveDays;
    private final Double attendanceRate;
    private final Double prescribedWorkingRatio;
}
