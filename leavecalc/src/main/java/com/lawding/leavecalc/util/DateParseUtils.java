package com.lawding.leavecalc.util;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DateParseUtils {


    /**
     * "MM-dd" 형식의 문자열을 {@link MonthDay}로 파싱합니다.
     * <p>
     * 프론트엔드에서 날짜 형식이 보장된 상태이며, 값이 없을 경우 {@code null}이 들어올 수 있는 상황을 가정합니다.
     * <p>
     * 입력값이 {@code null} 또는 빈 문자열인 경우 {@code null}을 반환합니다.
     *
     * @param fiscalYear "MM-dd" 형식의 문자열 (예: "05-10"), 또는 {@code null}
     * @return {@link MonthDay} 객체, 또는 {@code null} (입력값이 null이거나 빈 경우)
     */
    public static MonthDay parseNullableMonthDay(String fiscalYear) {
        if (fiscalYear == null || fiscalYear.isBlank()) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        return MonthDay.parse(fiscalYear, formatter);

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
