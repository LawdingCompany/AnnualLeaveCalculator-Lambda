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
    // 입사일 1년 미만 월차 루트가 아닌 경우 존재
    private final Double attendanceRate; // null 가능 -> 출근율 80% 미만이 흐름에 없었다면 null
    private final Double prescribedWorkingRatio; // null 가능 -> 출근율 80% 미만이 흐름에 없었다면 null
}
