package com.lawding.leavecalc.domain.flow.context;

import com.lawding.leavecalc.domain.DatePeriod;
import java.time.LocalDate;
import java.util.Set;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@ToString
public class MonthlyContext extends CalculationContext{
    private final DatePeriod accrualPeriod;
    private final DatePeriod availablePeriod;
    private final Set<LocalDate> absentDays;
    private final Set<LocalDate> excludedDays;
    private final Set<LocalDate> companyHolidays;
    private final Set<LocalDate> statutoryHolidays;
}
