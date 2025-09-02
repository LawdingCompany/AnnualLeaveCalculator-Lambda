package com.lawding.leavecalc.calculator.dispatcher;

import com.lawding.leavecalc.calculator.AnnualCalculator;
import com.lawding.leavecalc.calculator.MonthlyAndProratedCalculator;
import com.lawding.leavecalc.calculator.MonthlyCalculator;
import com.lawding.leavecalc.calculator.ProratedCalculator;
import com.lawding.leavecalc.domain.flow.FlowResult;
import com.lawding.leavecalc.domain.flow.context.AnnualContext;
import com.lawding.leavecalc.domain.flow.context.MonthlyAndProratedContext;
import com.lawding.leavecalc.domain.flow.context.MonthlyContext;
import com.lawding.leavecalc.domain.flow.context.ProratedContext;
import com.lawding.leavecalc.dto.detail.CalculationDetail;

public final class CalculationDispatcher {

    private static final MonthlyCalculator MONTHLY = new MonthlyCalculator();
    private static final AnnualCalculator ANNUAL = new AnnualCalculator();
    private static final ProratedCalculator PRORATED = new ProratedCalculator();
    private static final MonthlyAndProratedCalculator MONTHLY_AND_PRORATED =
        new MonthlyAndProratedCalculator(MONTHLY, PRORATED);
    private CalculationDispatcher() {
    }
    public static CalculationDetail calculate(FlowResult flowResult) {
        return switch (flowResult.getLeaveType()) {
            case MONTHLY -> MONTHLY.calculate((MonthlyContext) flowResult.getContext());
            case ANNUAL -> ANNUAL.calculate((AnnualContext) flowResult.getContext());
            case PRORATED -> PRORATED.calculate((ProratedContext) flowResult.getContext());
            case MONTHLY_AND_PRORATED ->
                MONTHLY_AND_PRORATED.calculate((MonthlyAndProratedContext) flowResult.getContext());
        };
    }
}
