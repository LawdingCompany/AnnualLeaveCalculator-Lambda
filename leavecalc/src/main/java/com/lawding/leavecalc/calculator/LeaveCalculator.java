package com.lawding.leavecalc.calculator;

import com.lawding.leavecalc.domain.flow.context.CalculationContext;
import com.lawding.leavecalc.domain.flow.detail.CalculationDetail;

public interface LeaveCalculator<T extends  CalculationContext> {

    CalculationDetail calculate(T context);
}
