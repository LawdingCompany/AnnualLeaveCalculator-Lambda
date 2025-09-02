package com.lawding.leavecalc.util;

import static com.lawding.leavecalc.constant.AnnualLeaveConstants.*;

import com.lawding.leavecalc.domain.DatePeriod;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class AnnualLeaveHelper {


    private AnnualLeaveHelper() {
    }

    /**
     * 근속연수에 따른 가산 연차를 계산하는 함수
     *
     * @param serviceYears 근속연수
     * @return 가산 연차 결과
     */
    public static int calculateAdditionalLeave(int serviceYears) {
        return serviceYears < 1 ? 0 : Math.min((serviceYears - 1) / 2, MAX_ADDTIONAL_LEAVE);
    }


    /**
     * 입사 후 1년 미만인지 확인하는 함수
     *
     * @param hireDate      입사일
     * @param referenceDate 기준일
     * @return 근속연수가 1년 미만인가
     */
    public static boolean isBeforeOneYearFromHireDate(LocalDate hireDate, LocalDate referenceDate) {
        return referenceDate.isBefore(hireDate.plusYears(1));
    }

    /**
     * 해당 날짜가 주말(토,일)인지 아닌지 판단하는 함수
     *
     * @param date 날짜
     * @return 주말이 아닌지 판단
     */
    public static boolean isWeekday(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
    }


    /**
     * @param value 비율 * 연차
     * @return 소수점 둘째자리 까지 올림한 결과
     */
    public static double formatDouble(double value) {
        return Math.ceil(value * 100) / 100;
    }


    private static DatePeriod intersectPeriod(DatePeriod p1, DatePeriod p2) {
        LocalDate start = p1.startDate().isAfter(p2.startDate()) ? p1.startDate() : p2.startDate();
        LocalDate end = p1.endDate().isBefore(p2.endDate()) ? p1.endDate() : p2.endDate();
        return end.isBefore(start) ? null : new DatePeriod(start, end);
    }


    /**
     * @param prescribedWorkingDays 소정근로일 수(연차 산정 기간에서의 근무날 수)
     * @param excludedWorkingDays   소정근로제외일 수
     * @return PWR(소정근로비율) = (소정근로일 수 - 소정근로제외일 수) / 소정근로일 수
     */
    public static double calculatePrescribedWorkingRatio(int prescribedWorkingDays,
        int excludedWorkingDays) {
        if (prescribedWorkingDays <= 0) {
            return 0;
        }
        int numerator = prescribedWorkingDays - excludedWorkingDays;
        //  소정근로일 수 - 소정근로제외일 수
        return (double) numerator / prescribedWorkingDays;
    }

    /**
     * @param prescribedWorkingDays 소정근로일 수(연차 산정 기간에서의 근무날 수)
     * @param absentDays            결근처리일 수
     * @param excludedWorkingDays   소정근로일제외일수
     * @return AR(출근율) = (소정 근로일 수 - 소정근로 제외일 수 - 결근처리 일 수) / (소정근로일 수 - 소정근로제외 일 수)
     */
    public static double calculateAttendanceRate(int prescribedWorkingDays,
        int absentDays, int excludedWorkingDays) {

        int numerator = prescribedWorkingDays - excludedWorkingDays - absentDays;
        // 소정 근로일 수 - 소정근로 제외일 수 - 결근처리 일 수

        int denominator = prescribedWorkingDays - excludedWorkingDays;
        // 소정근로일 수 - 소정근로제외일 수

        if (denominator <= 0) {
            return 0;
        }
        return (double) numerator / denominator;
    }

    /**
     * 산정 기간 내 기본 소정근로일 수를 계산한다. - 소정근로일 : 주말(토/일)과 법정공휴일을 제외한 평일
     *
     * @param accrualPeriod     산정 기간 [startDate, endDate] 포함
     * @param statutoryHolidays 법정공휴일 목록 (null 가능)
     * @return 소정근로일 수
     */
    public static int countPrescribedWorkingDays(DatePeriod accrualPeriod,
        Set<LocalDate> statutoryHolidays) {

        Set<LocalDate> holidays = (statutoryHolidays == null) ? Set.of() : statutoryHolidays;

        long days = accrualPeriod.startDate().datesUntil(accrualPeriod.endDate().plusDays(1))
            .filter(AnnualLeaveHelper::isWeekday)
            .filter(day -> !holidays.contains(day))
            .count();

        return (int) days;
    }

    /**
     * 산정 기간 내 기간(결근처리기간 or 소정근로제외기간) 내 평일 수를 계산한다. - 제외기간과 산정기간의 교집합 구간에서 주말/법정공휴일을 제외한 평일만 센다.
     *
     * @param accrualPeriod     연차 산정 기간
     * @param periods           기간(결근처리, 소정근로제외)
     * @param statutoryHolidays 법정 공휴일
     * @return 순수 소정근로일(평일) 수
     */
    public static Set<LocalDate> getWorkingDaysInPeriods(DatePeriod accrualPeriod,
        List<DatePeriod> periods, Set<LocalDate> statutoryHolidays) {
        if (periods == null || periods.isEmpty()) {
            return Set.of();
        }

        Set<LocalDate> holidays = (statutoryHolidays == null) ? Set.of() : statutoryHolidays;

        return periods.stream()
            .map(p -> intersectPeriod(p, accrualPeriod)) // 교집합
            .filter(Objects::nonNull)
            .flatMap(p -> p.startDate().datesUntil(p.endDate().plusDays(1)))
            .filter(AnnualLeaveHelper::isWeekday)        // 주말 제외
            .filter(d -> !holidays.contains(d))        // 공휴일 제외
            .collect(Collectors.toSet());
    }

    /**
     * 산정 기간 내 특정 일자(LocalDate 리스트) 중 평일 수를 계산한다. - 산정 기간(accrualPeriod)에 속하는 날짜만 반영 - 주말/법정공휴일 제외
     *
     * @param accrualPeriod     연차 산정 기간
     * @param companyHoliday    특정 일자 리스트 (예: 회사 지정 휴일)
     * @param statutoryHolidays 법정 공휴일
     * @return 순수 소정근로일(평일) 집합
     */
    public static Set<LocalDate> getWorkingDaysInCompanyHolidays(DatePeriod accrualPeriod,
        List<LocalDate> companyHoliday, Set<LocalDate> statutoryHolidays) {

        if (companyHoliday == null || companyHoliday.isEmpty()) {
            return Set.of();
        }

        Set<LocalDate> holidays = (statutoryHolidays == null) ? Set.of() : statutoryHolidays;

        return companyHoliday.stream()
            .filter(
                d -> !d.isBefore(accrualPeriod.startDate()) && !d.isAfter(accrualPeriod.endDate()))
            .filter(AnnualLeaveHelper::isWeekday)
            .filter(d -> !holidays.contains(d))
            .collect(Collectors.toSet());
    }

}
