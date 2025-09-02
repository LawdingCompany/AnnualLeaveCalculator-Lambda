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
    private final Set<LocalDate> absentDays;
    private final Set<LocalDate> excludedDays;
    private final Set<LocalDate> companyHolidays;
    private final Set<LocalDate> statutoryHolidays;
    private final Double attendanceRate; // null 가능 -> 출근율 80% 미만이 흐름에 없었다면 null
}
