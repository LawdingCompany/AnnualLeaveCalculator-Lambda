package com.lawding.leavecalc.domain.flow.context;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@ToString
public abstract class CalculationContext {
    private final int serviceYears;
}
