package com.lawding.leavecalc.domain.flow;

import com.lawding.leavecalc.domain.DatePeriod;
import java.time.LocalDate;
import java.util.Set;

public interface MonthlyCalcContext {
    DatePeriod getAccrualPeriod();
    Set<LocalDate> getAbsentDays();
    Set<LocalDate> getExcludedDays();
    Set<LocalDate> getHolidays();
}
