package com.lawding.leavecalc.dto.detail;

import com.lawding.leavecalc.domain.flow.context.CalculationContext;
import com.lawding.leavecalc.domain.flow.context.MonthlyContext;
import lombok.Builder;
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
