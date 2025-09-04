package com.lawding.leavecalc.domain.flow.detail;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lawding.leavecalc.domain.DatePeriod;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@SuperBuilder
@ToString
public abstract class CalculationDetail {
    private final DatePeriod accrualPeriod;
    private final DatePeriod availablePeriod;
    private final Double attendanceRate;
    private final Double prescribedWorkingRatio;
    private final double totalLeaveDays;
}
