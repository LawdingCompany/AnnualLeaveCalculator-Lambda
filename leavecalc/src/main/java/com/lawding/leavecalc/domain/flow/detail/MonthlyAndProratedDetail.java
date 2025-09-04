package com.lawding.leavecalc.domain.flow.detail;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@ToString
public class MonthlyAndProratedDetail extends CalculationDetail {
    private final MonthlyDetail monthlyDetail;
    private final CalculationDetail proratedDetail;
}
