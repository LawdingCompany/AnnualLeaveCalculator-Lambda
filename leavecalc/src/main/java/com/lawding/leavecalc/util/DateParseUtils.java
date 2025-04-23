package com.lawding.leavecalc.util;

import com.lawding.leavecalc.domain.DateRange;
import com.lawding.leavecalc.dto.DateRangeRequest;
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

    public static List<DateRange> convertToDateRanges(List<DateRangeRequest> requests) {
        if (requests == null) {
            return null;
        }
        return requests.stream()
            .map(request -> new DateRange(
                LocalDate.parse(request.startDate()),
                LocalDate.parse(request.endDate())
            ))
            .toList();
    }
}
