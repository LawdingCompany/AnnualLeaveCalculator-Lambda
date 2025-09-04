package com.lawding.leavecalc.domain.flow.context;

import com.lawding.leavecalc.domain.DatePeriod;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@ToString
public abstract class CalculationContext {
    private final int serviceYears;
    private final DatePeriod accrualPeriod;
    private final DatePeriod availablePeriod;
    private final Double attendanceRate;
    private final Double prescribedWorkingRatio;
}
