package com.lawding.leavecalc.util;

import com.lawding.leavecalc.domain.DatePeriod;
import com.lawding.leavecalc.domain.NonWorkingPeriod;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Map;

public class AnnualLeaveHelper {

    private AnnualLeaveHelper() {
    }

    public static boolean isLessThanOneYear(LocalDate hireDate, LocalDate referenceDate) {
        return referenceDate.isBefore(hireDate.plusYears(1));
    }

    public static Period getPeriod(LocalDate startDate, LocalDate endDate) {
        return Period.between(startDate, endDate);
    }

    public static boolean isFullAttendance(LocalDate startDate, LocalDate endDate,
        List<DatePeriod> excludedPeriods) {
        return excludedPeriods.stream()
            .noneMatch(period ->
                !(period.endDate().isBefore(startDate) || period.startDate().isAfter(endDate))
            );
    }

}
