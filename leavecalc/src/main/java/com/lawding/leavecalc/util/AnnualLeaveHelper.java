package com.lawding.leavecalc.util;

import com.lawding.leavecalc.domain.AnnualLeaveContext;
import com.lawding.leavecalc.domain.DatePeriod;
import com.lawding.leavecalc.domain.NonWorkingPeriod;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Period;
import java.time.Year;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AnnualLeaveHelper {

    private static final int BASE_ANNUAL_LEAVE = 15;
    private static final int MAX_ANNUAL_LEAVE = 25;

    private AnnualLeaveHelper() {
    }

    // 근속년수가 1년 미만인지 확인하는 함수
    public static boolean isLessThanOneYear(LocalDate hireDate, LocalDate referenceDate) {
        return referenceDate.isBefore(hireDate.plusYears(1));
    }

    public static Period getPeriod(LocalDate startDate, LocalDate endDate) {
        return Period.between(startDate, endDate);
    }

    public static int calculateLeaveByServiceYears(LocalDate startDate, LocalDate endDate) {
        int serviceYears = getPeriod(startDate, endDate).getYears();
        if (serviceYears < 1) {
            return 0;
        }
        int additional = (serviceYears - 1) / 2;
        return Math.min(BASE_ANNUAL_LEAVE + additional, MAX_ANNUAL_LEAVE);
    }

    public static boolean isFullAttendance(LocalDate startDate, LocalDate endDate,
        List<DatePeriod> excludedPeriods) {
        return excludedPeriods.stream()
            .noneMatch(period ->
                !(period.endDate().isBefore(startDate) || period.startDate().isAfter(endDate))
            );
    }

    /**
     * @param hireDate
     * @param referenceDate
     * @return 연차 산정 단위 기간 [시작일, 종료일]
     */
    public static DatePeriod getPreviousAccrualPeriod(LocalDate hireDate, LocalDate referenceDate) {
        // 만약에 1년 미만인 경우 => 들어오지 않음
        // 만약에 1년 이상인 경우 => 2024-02-11 , 2025-06-10 => 직전연차산정기간 [2024-02-11,2025-02-11]
        int years = getPeriod(hireDate, referenceDate).getYears();
        LocalDate start = hireDate.plusYears(years - 1);
        LocalDate end = start.plusYears(1).minusDays(1);
        return new DatePeriod(start, end);
    }

    /**
     * @param startDate 산정 시작일
     * @param endDate 산정 종료일
     * @param context
     * @return 소정 근로일 수 = 전체 기간 일수 - 주말(토,일) - 법정 공휴일 - 회사 휴일(창립기념일)
     */
    public static int calculatePrescribedWorkingDays(LocalDate startDate, LocalDate endDate,
        AnnualLeaveContext context, List<LocalDate> statutoryHolidays) {
        Set<LocalDate> excludedDays = new HashSet<>(statutoryHolidays);
        excludedDays.addAll(context.getCompanyHolidays());
        return (int) startDate.datesUntil(endDate.plusDays(1))
            .filter(AnnualLeaveHelper::isWeekday) // 주말(토, 일)
            .filter(date -> !excludedDays.contains(date)) // 법정 공휴일 & 회사 휴일
            .count();
    }

    private static boolean isWeekday(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
    }

    /**
     * @return
     * @Param originalWorkdays : 소정근로일 수(연차 산정 기간에서의 평일 수)
     * @Param extra : 비근무 기간 배열
     * <p>
     * 출근 간주 / 결근 처리 / 소정근로제외 형태 중 출근 간주는 이미 반영되어있어 중복 반영x 만약 소정근로일 수 <= 소정근로제외일수 일 경우 AR = 1.0 그 외)
     * AR = (소정 근로일 수 - 결근처리 일 수) / (소정근로일 수 - 소정근로제외 일 수)
     */
    public static double calculateAttendanceRate(int prescribedWorkingDays) {
    }

    /**
     * @return
     * @Param originalWorkdays : 소정근로일 수(연차 산정 기간에서의 평일 수)
     * @Param extra : 비근무 기간 배열
     * <p>
     * (소정근로제외일 수 / 소정근로일 수) <= 0.2 이면 ASR = 1.0 그게 아니면 PWR = (소정근로일 수 - 소정근로제외일 수) / 소정근로일 수
     */
    public static double calculatePrescribedWorkingRatio(int prescribedWorkingDays) {
    }

}
