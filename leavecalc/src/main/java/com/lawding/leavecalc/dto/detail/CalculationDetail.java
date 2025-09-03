package com.lawding.leavecalc.dto.detail;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@ToString
public abstract class CalculationDetail {
    private final double totalLeaveDays;
}
