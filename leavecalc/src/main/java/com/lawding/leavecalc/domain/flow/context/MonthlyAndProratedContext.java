package com.lawding.leavecalc.domain.flow.context;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@ToString
public class MonthlyAndProratedContext extends CalculationContext{
    private final MonthlyContext monthlyContext;
    private final ProratedContext proratedContext;
}
