package com.lawding.leavecalc.util;

import static com.lawding.leavecalc.constant.AnnualLeaveConstants.*;

import com.lawding.leavecalc.domain.DatePeriod;
import com.lawding.leavecalc.domain.detail.MonthlyLeaveDetail;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AnnualLeaveHelper {


    private AnnualLeaveHelper() {
    }

    /**
     * 입사 후 1년 미만인지 확인하는 함수
     *
     * @param hireDate      입사일
     * @param referenceDate 기준일
     * @return 근속연수가 1년 미만인가
     */
    public static boolean isLessThanOneYear(LocalDate hireDate, LocalDate referenceDate) {
        return referenceDate.isBefore(hireDate.plusYears(1));
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
     * 해당 날짜가 주말(토,일)인지 아닌지 판단하는 함수
     *
     * @param date 날짜
     * @return 주말이 아닌지 판단
     */
    static boolean isWeekday(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
    }


    /**
     * @param prescribedWorkingDays 소정근로일 수(연차 산정 기간에서의 근무날 수)
     * @param absentDays            결근처리일 수
     * @param excludedWorkingDays   소정근로일제외일수
     * @return AR(출근율) = (소정 근로일 수 - 소정근로 제외 일수 - 결근처리 일 수) / (소정근로일 수 - 소정근로제외 일 수)
     */
    public static double calculateAttendanceRate(int prescribedWorkingDays,
        int absentDays, int excludedWorkingDays) {
        int numerator = prescribedWorkingDays - excludedWorkingDays - absentDays;
        int denominator = prescribedWorkingDays - excludedWorkingDays;
        if (denominator <= 0) {
            return 0;
        }
        return (double) numerator / denominator;

    }

    /**
     * @param period          [시작일, 종료일]
     * @param excludedPeriods 결근 처리 기간을 저장한 배열
     * @return 종료일 다음 날에 발생하는 월차 계산 함수 (최대 11개)
     * <p>
     * 해당 함수는 종료일을 포함해서 계산하기에 해당 월차는 종료일 + 1일 후의 발생하는 월차를 계산한다. 즉, referenceDate를 종료일로 넣을 경우,
     * referenceDate.plusDays(1)일 후에 발생하는 월차를 계산하므로 endDate = referenceDate.minusDays(1)이다.
     */
    public static MonthlyLeaveDetail monthlyAccruedLeaves(DatePeriod period,
        List<DatePeriod> excludedPeriods) {
        List<MonthlyLeaveDetail.MonthlyLeaveGrantRecord> records = new ArrayList<>();
        int totalMonthlyLeaves = 0;
        LocalDate currentStart = period.startDate();

        while (totalMonthlyLeaves < MAX_MONTHLY_LEAVE && !currentStart.isAfter(period.endDate())) {
            LocalDate currentEnd = currentStart.plusMonths(1).minusDays(1);

            boolean fullAttendance = isFullAttendance(currentStart, currentEnd, excludedPeriods);
            double granted = fullAttendance ? 1 : 0;

            records.add(MonthlyLeaveDetail.MonthlyLeaveGrantRecord.builder()
                .period(new DatePeriod(currentStart, currentEnd))
                .monthlyLeave(granted)
                .build());

            if (fullAttendance) {
                totalMonthlyLeaves++;
            }
            currentStart = currentEnd.plusDays(1);
        }

        return MonthlyLeaveDetail.builder()
            .records(records)
            .totalLeaveDays(totalMonthlyLeaves)
            .build();
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

    /**
     * @param value 비율 * 연차
     * @return 소수점 둘째자리 까지 올림한 결과
     */
    public static double formatDouble(double value) {
        return Math.ceil(value * 100) / 100;
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
     * @param periods           기간들을 저장하고 있는 리스트
     * @param standard          연차 산정 기간
     * @param companyHolidays   회사 휴일
     * @param statutoryHolidays 법정 공휴일
     * @return 연차 산정 기간과 겹치는 결근/소정근로제외일 중 소정근로일(주말·공휴일 제외) 계산
     */
    public static int calculatePrescribedWorkingDaysWithinPeriods(List<DatePeriod> periods,
        DatePeriod standard,
        List<LocalDate> companyHolidays, List<LocalDate> statutoryHolidays) {
        Set<LocalDate> holidays = Stream.concat(companyHolidays.stream(),
                statutoryHolidays.stream())
            .collect(Collectors.toSet());

        Set<LocalDate> dates = periods.stream()
            .map(p -> intersectPeriod(p, standard))
            .filter(Objects::nonNull)
            .flatMap(p -> p.startDate().datesUntil(p.endDate().plusDays(1)))
            .filter(d -> isWeekday(d) && !holidays.contains(d))
            .collect(Collectors.toSet());

        return dates.size();
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
        return (double) numerator / prescribedWorkingDays;
    }


}
