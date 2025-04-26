package com.lawding.leavecalc.util;

import static com.lawding.leavecalc.constant.AnnualLeaveConstants.*;

import com.lawding.leavecalc.domain.AnnualLeaveContext;
import com.lawding.leavecalc.domain.DatePeriod;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AnnualLeaveHelper {


    private AnnualLeaveHelper() {
    }

    public static int caculateDaysBetween(DatePeriod period) {
        return (int) ChronoUnit.DAYS.between(period.startDate(), period.endDate());
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
     * 소정근로일을 계산하는 함수
     *
     * @param period            [시작일, 종료일]
     * @param companyHolidays   회사 휴일(창립기념일)
     * @param statutoryHolidays 법정 공휴일을 저장한 배열
     * @return 소정 근로일 수 = 전체 기간 일수 - 주말(토,일) - 법정 공휴일 - 회사 휴일
     */
    public static int calculatePrescribedWorkingDays(DatePeriod period,
        List<LocalDate> companyHolidays, List<LocalDate> statutoryHolidays) {
        Set<LocalDate> excludedDays = new HashSet<>(statutoryHolidays);
        excludedDays.addAll(companyHolidays);
        return (int) period.startDate().datesUntil(period.endDate().plusDays(1))
            .filter(AnnualLeaveHelper::isWeekday) // 주말(토, 일)
            .filter(date -> !excludedDays.contains(date)) // 법정 공휴일 & 회사 휴일
            .count();
    }

    /**
     * 해당 날짜가 주말(토,일)인지 아닌지 판단하는 함수
     *
     * @param date 날짜
     * @return 주말이 아닌지 판단
     */
    private static boolean isWeekday(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
    }

    /**
     * @param periods           기간들이 저장되어 있는 배열(List)
     * @param companyHolidays   회사 휴일(창립기념일)
     * @param statutoryHolidays 법정 공휴일을 저장한 배열
     * @return sum(해당 기간의 소정근로일 수 ( 기간 - 주말 - 법정 공휴일 - 회사 휴일))
     */
    public static int calculateTotalDaysFromPeriods(List<DatePeriod> periods,
        List<LocalDate> companyHolidays, List<LocalDate> statutoryHolidays) {
        Set<LocalDate> excludedDays = new HashSet<>(statutoryHolidays);
        excludedDays.addAll(companyHolidays);

        return periods.stream()
            .mapToInt(period ->
                (int) period.startDate().datesUntil(period.endDate().plusDays(1)) // 종료일 포함
                    .filter(AnnualLeaveHelper::isWeekday)                         // 주말 제외
                    .filter(date -> !excludedDays.contains(date))                // 공휴일 제외
                    .count()
            )
            .sum();
    }

    /**
     * @param prescribedWorkingDays 소정근로일 수(연차 산정 기간에서의 근무날 수)
     * @param nonWorkingPeriods     타입(1,2,3)에 따라 비근무 기간을 저정한 배열
     *                              <p>
     *                              출근 간주 / 결근 처리 / 소정근로제외 형태 중 출근 간주는 이미 반영되어있어 중복 반영x 만약 소정근로일  <=
     *                              소정근로제외일수 일 경우 AR = 1.0 그 외)
     * @return AR(출근율) = (소정 근로일 수 - 결근처리 일 수) / (소정근로일 수 - 소정근로제외 일 수)
     */
    public static double calculateAttendanceRate(int prescribedWorkingDays,
        Map<Integer, List<DatePeriod>> nonWorkingPeriods, List<LocalDate> companyHolidays,
        List<LocalDate> statutoryHolidays) {
        List<DatePeriod> absentPeriods = nonWorkingPeriods.getOrDefault(2, List.of());
        List<DatePeriod> excludedWorkPeriods = nonWorkingPeriods.getOrDefault(3, List.of());
        int absentDays = calculateTotalDaysFromPeriods(absentPeriods, companyHolidays,
            statutoryHolidays); // 결근 처리된 소정 근로일 수
        int excludeWorkingDays = calculateTotalDaysFromPeriods(excludedWorkPeriods, companyHolidays,
            statutoryHolidays); // 소정근로제외일 처리 된 소정 근로일 수
        int numerator = prescribedWorkingDays - absentDays;
        int denominator = prescribedWorkingDays - excludeWorkingDays;
        if (denominator <= 0) {
            return 0;
        }
        return formatDouble((double) numerator / denominator);

    }

    public static int monthlyAccruedLeaves(LocalDate startDate, LocalDate endDate,
        List<DatePeriod> excludedPeriods) {
        int accruedLeaves = 0;
        LocalDate periodStart = startDate;
        while (true) {
            LocalDate periodEnd = periodStart.plusMonths(1).minusDays(1);
            LocalDate accrualDate = periodEnd.plusDays(1);

            if (accrualDate.isAfter(endDate)) {
                break;
            }
            if (isFullAttendance(periodStart, periodEnd, excludedPeriods)) {
                accruedLeaves++;
            }
            periodStart = accrualDate;
        }
        return accruedLeaves;
    }

    public static double formatDouble(double value) {
        return Math.ceil(value * 100) / 100;
    }

    /**
     * [시작일, 종료일] 에 결근한 날이 있는지 확인하며 개근을 판단하는 함수
     *
     * @param startDate       시작일
     * @param endDate         종료일
     * @param excludedPeriods 결근 처리 형태의 비근무 기간 배열
     * @return [시작일, 종료일]  개근 판단
     */
    private static boolean isFullAttendance(LocalDate startDate, LocalDate endDate,
        List<DatePeriod> excludedPeriods) {
        return excludedPeriods.stream()
            .allMatch(period ->
                period.endDate().isBefore(startDate) || period.startDate().isAfter(endDate)
            );
    }

}
