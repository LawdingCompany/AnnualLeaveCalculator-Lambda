package com.lawding.leavecalc.strategy;

import com.lawding.leavecalc.domain.AnnualLeaveContext;

public class CalculationStrategyFactory {

    private static final HireDateStrategy HIRE_DATE_STRATEGY = new HireDateStrategy();
    private static final FiscalYearStrategy FISCAL_YEAR_STRATEGY = new FiscalYearStrategy();

    public static CalculationStrategy from(AnnualLeaveContext context) {
        return switch (context.calculationType()) {
            case HIRE_DATE -> HIRE_DATE_STRATEGY;
            case FISCAL_YEAR -> FISCAL_YEAR_STRATEGY;
        };
    }

}
