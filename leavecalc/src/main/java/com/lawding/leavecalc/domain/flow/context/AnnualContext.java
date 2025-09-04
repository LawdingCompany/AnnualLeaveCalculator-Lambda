package com.lawding.leavecalc.domain.flow.context;

import com.lawding.leavecalc.domain.DatePeriod;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@ToString
public class AnnualContext extends CalculationContext {
    private final DatePeriod accrualPeriod;
    private final DatePeriod availablePeriod;
}
