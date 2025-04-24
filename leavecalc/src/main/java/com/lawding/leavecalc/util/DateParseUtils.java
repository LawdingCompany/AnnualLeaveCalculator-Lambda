package com.lawding.leavecalc.util;

import com.lawding.leavecalc.domain.NonWorkingPeriod;
import com.lawding.leavecalc.dto.NonWorkingPeriodRequest;
import java.time.LocalDate;
import java.util.List;

public class DateParseUtils {

    public static LocalDate parseNullable(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        return LocalDate.parse(dateStr);
    }

    public static List<LocalDate> convertToLocalDates(List<String> dates) {
        if (dates == null) {
            return null;
        }
        return dates.stream()
            .map(LocalDate::parse)
            .toList();
    }
}
