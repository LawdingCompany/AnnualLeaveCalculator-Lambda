package com.lawding.leavecalc.strategy.factory;

import com.lawding.leavecalc.domain.AnnualLeaveContext;
import com.lawding.leavecalc.flow.FiscalYearFlow;
import com.lawding.leavecalc.flow.HireDateFlow;
import com.lawding.leavecalc.repository.HolidayJdbcRepository;
import com.lawding.leavecalc.strategy.CalculationStrategy;
import com.lawding.leavecalc.strategy.FiscalYearStrategy;
import com.lawding.leavecalc.strategy.HireDateStrategy;

public class CalculationStrategyFactory {

    private static final HolidayJdbcRepository holidayRepository = new HolidayJdbcRepository();
    private static final HireDateStrategy HIRE_DATE_STRATEGY = new HireDateStrategy(
        new HireDateFlow(holidayRepository));
    private static final FiscalYearStrategy FISCAL_YEAR_STRATEGY = new FiscalYearStrategy(
        new FiscalYearFlow(holidayRepository));

    public static CalculationStrategy from(AnnualLeaveContext context) {
        return switch (context.getCalculationType()) {
            case HIRE_DATE -> HIRE_DATE_STRATEGY;
            case FISCAL_YEAR -> FISCAL_YEAR_STRATEGY;
        };
    }

}
