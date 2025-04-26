package com.lawding.leavecalc.strategy;

import com.lawding.leavecalc.domain.AnnualLeaveContext;
import com.lawding.leavecalc.repository.HolidayJdbcRepository;

public class CalculationStrategyFactory {

    private static final HolidayJdbcRepository holidayRepository = new HolidayJdbcRepository();
    private static final HireDateStrategy HIRE_DATE_STRATEGY = new HireDateStrategy(
        holidayRepository);
    private static final FiscalYearStrategy FISCAL_YEAR_STRATEGY = new FiscalYearStrategy(
        holidayRepository);

    public static CalculationStrategy from(AnnualLeaveContext context) {
        return switch (context.getCalculationType()) {
            case HIRE_DATE -> HIRE_DATE_STRATEGY;
            case FISCAL_YEAR -> FISCAL_YEAR_STRATEGY;
        };
    }

}
