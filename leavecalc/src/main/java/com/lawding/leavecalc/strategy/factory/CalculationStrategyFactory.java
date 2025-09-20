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
    public static CalculationStrategy from(AnnualLeaveContext context) {
        return switch (context.getCalculationType()) {
            case HIRE_DATE ->
                new HireDateStrategy(new HireDateFlow(holidayRepository));
            case FISCAL_YEAR ->
                new FiscalYearStrategy(new FiscalYearFlow(holidayRepository));
        };
    }

}
