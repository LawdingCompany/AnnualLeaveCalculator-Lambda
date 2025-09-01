package com.lawding.leavecalc.util;

import static com.lawding.leavecalc.constant.AnnualLeaveConstants.*;

import com.lawding.leavecalc.domain.DatePeriod;
import com.lawding.leavecalc.domain.flow.context.CalculationContext;
import com.lawding.leavecalc.domain.flow.context.MonthlyContext;
import com.lawding.leavecalc.dto.detail.MonthlyLeaveDetail;
import com.lawding.leavecalc.domain.flow.MonthlyCalcContext;
import com.lawding.leavecalc.domain.MonthlyLeaveRecord;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
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
    static boolean isWeekday(LocalDate date) {
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
        return (double) numerator / prescribedWorkingDays;
    }


    /**
     * @param dates         날짜가 들어있는 리스트
     * @param holidays      공휴일 리스트
     * @param excludedDates 소정근로제외일 리스트
     * @return 날짜들 중 주말, 공휴일, 소정근로제외일을 제외한 날 반환
     */
    private static Set<LocalDate> filterPrescribedWorkingDays(Set<LocalDate> dates,
        Set<LocalDate> holidays, Set<LocalDate> excludedDates) {
        return dates.stream()
            .filter(AnnualLeaveHelper::isWeekday)
            .filter(day -> !holidays.contains(day))
            .filter(day -> !excludedDates.contains(day))
            .collect(Collectors.toSet());
    }

    /**
     * @param periods  기간이 들어있는 리스트
     * @param standard 기준 기간
     * @return 기준 기간에 해당하는 날짜들을 추출
     */
    private static Set<LocalDate> extractDatesWithinPeriod(List<DatePeriod> periods,
        DatePeriod standard) {
        return periods.stream()
            .map(period -> intersectPeriod(period, standard))
            .filter(Objects::nonNull)
            .flatMap(period -> period.startDate().datesUntil(period.endDate().plusDays(1)))
            .collect(Collectors.toSet());
    }


    /**
     * @param accrualPeriods    연차산정기간
     * @param statutoryHolidays 법정공휴일
     * @param companyHolidays   회사휴일
     * @return 연차산정기간의 전체 소정근로일 계산
     */
    public static int calculatePrescribedWorkingDays(DatePeriod accrualPeriods,
        List<LocalDate> statutoryHolidays, List<LocalDate> companyHolidays) {
        Set<LocalDate> holidays = new HashSet<>(statutoryHolidays);
        holidays.addAll(companyHolidays);

        Set<LocalDate> workingDays = extractDatesWithinPeriod(accrualPeriods);
        return filterPrescribedWorkingDays(workingDays, holidays, Set.of()).size();
    }

    /**
     * @param absentPeriods     결근처리기간
     * @param standard          기준 기간(시작일, 종료일)
     * @param statutoryHolidays 법정공휴일
     * @param companyHolidays   회사휴일
     * @param excludedPeriods   소정근로제외일
     * @return 결근처리기간들 중 기준 기간 내 소정근로일 추출(소정근로 제외일도 고려함)
     */
    public static Set<LocalDate> getPrescribedWorkingDaySetInAbsentPeriods(
        List<DatePeriod> absentPeriods,
        DatePeriod standard, List<LocalDate> statutoryHolidays, List<LocalDate> companyHolidays,
        List<DatePeriod> excludedPeriods) {
        Set<LocalDate> holidays = new HashSet<>(statutoryHolidays);
        holidays.addAll(companyHolidays);

        Set<LocalDate> absentDays = extractDatesWithinPeriod(absentPeriods, standard);
        Set<LocalDate> excludedDays = extractDatesWithinPeriod(excludedPeriods, standard);

        return filterPrescribedWorkingDays(absentDays, holidays, excludedDays);
    }


    /**
     * @param excludedPeriods   소정근로제외기간
     * @param standard          기준 기간(시작일, 종료일)
     * @param statutoryHolidays 법정공휴일
     * @param companyHolidays   회사휴일
     * @return 소정근로제외기간들 중 기준 기간 내 소정근로일 계산
     */
    public static int calculateExcludedWorkingDays(List<DatePeriod> excludedPeriods,
        DatePeriod standard, List<LocalDate> statutoryHolidays, List<LocalDate> companyHolidays) {
        Set<LocalDate> holidays = new HashSet<>(statutoryHolidays);
        holidays.addAll(companyHolidays);

        Set<LocalDate> excludedDays = extractDatesWithinPeriod(excludedPeriods, standard);

        return filterPrescribedWorkingDays(excludedDays, holidays, Set.of()).size();
    }

    //-------------------------------------------------------------------------

    /**
     * @param accrualPeriod     산정 기간
     * @param periods           결근처리 기간 or 소정근로제외 기간
     * @param companyHolidays   회사 휴일
     * @param statutoryHolidays 법정 공휴일
     * @return 산정 기간 내 기간 중 회사 휴일과 법정 공휴일, 주말을 제외한 결근 일을 담은 Set periods 중 순수한 날을 담은 Set
     */
    public static Set<LocalDate> getPrescribedWorkingDayInPeriods(DatePeriod accrualPeriod,
        List<DatePeriod> periods, List<LocalDate> companyHolidays,
        List<LocalDate> statutoryHolidays) {
        Set<LocalDate> days = extractDatesWithinPeriod(periods, accrualPeriod);
        Set<LocalDate> holidays = new HashSet<>(companyHolidays);
        holidays.addAll(statutoryHolidays);
        return filterPrescribedWorkingDays(days, holidays);
    }


    /**
     * @param accrualPeriod     산정 기간
     * @param companyHolidays   회사 휴일
     * @param statutoryHolidays 법정 공휴일
     * @return 산정 기간 중 회사 휴일과 법정 공휴일, 주말을 제외한 결근 일을 담은 Set periods 중 순수한 날을 담은 Set
     */
    public static Set<LocalDate> getPrescribedWorkingDayInHolidays(DatePeriod accrualPeriod,
        List<LocalDate> companyHolidays, List<LocalDate> statutoryHolidays) {
        Set<LocalDate> holidays = new HashSet<>();
        if (statutoryHolidays != null) {
            holidays.addAll(statutoryHolidays);
        }
        if (companyHolidays != null) {
            holidays.addAll(companyHolidays);
        }

        return holidays.stream()
            .filter(day -> isWithinInclusive(day, accrualPeriod))
            .filter(AnnualLeaveHelper::isWeekday)
            .collect(Collectors.toSet());
    }

    /**
     * @param day    날짜
     * @param period 기간
     * @return 해당 날짜가 기간 안에 포함되는지 여부
     */
    private static boolean isWithinInclusive(LocalDate day, DatePeriod period) {
        return !day.isBefore(period.startDate()) && !day.isAfter(period.endDate());
    }

    /**
     * @param days     날짜가 들어있는 리스트
     * @param holidays 공휴일(법정공휴일 + 회사휴일) 리스트
     * @return 날짜들 중 주말, 공휴일(법정공휴일 + 회사휴일)을 제외한 날을 담은 Set
     */
    private static Set<LocalDate> filterPrescribedWorkingDays(Set<LocalDate> days,
        Set<LocalDate> holidays) {
        return days.stream()
            .filter(AnnualLeaveHelper::isWeekday)
            .filter(day -> !holidays.contains(day))
            .collect(Collectors.toSet());
    }

    public static MonthlyLeaveDetail monthlyAccruedLeaves(MonthlyContext context) {
        DatePeriod period = context.getAccrualPeriod();
        Set<LocalDate> absentDays = context.getAbsentDays();
        Set<LocalDate> excludedDays = context.getExcludedDays();
        Set<LocalDate> holidays = context.getHolidays();

        List<MonthlyLeaveRecord> records = new ArrayList<>();
        double totalMonthlyLeaves = 0.0;

        LocalDate currentStart = period.startDate();

        while (totalMonthlyLeaves < MAX_MONTHLY_LEAVE) {
            LocalDate currentEnd = currentStart.plusMonths(1).minusDays(1);

            if (currentEnd.isAfter(period.endDate())) {
                break;
            }

            // 간격 내 소정근로일
            Set<LocalDate> prescribedSet = currentStart
                .datesUntil(currentEnd.plusDays(1))
                .filter(AnnualLeaveHelper::isWeekday)
                .filter(day -> !holidays.contains(day))
                .collect(Collectors.toSet());

            int denominator = prescribedSet.size();
            double granted = 0.0;

            if (denominator > 0) {

                boolean hasAbsence = absentDays.stream()
                    .anyMatch(prescribedSet::contains);

                if (!hasAbsence) {
                    int excludedDay = (int) excludedDays.stream()
                        .filter(prescribedSet::contains)
                        .count();

                    int attendanceDays = denominator - excludedDay;
                    granted = (double) attendanceDays / denominator;
                }

            }

            records.add(
                MonthlyLeaveRecord.builder()
                    .period(new DatePeriod(currentStart, currentEnd))
                    .monthlyLeave(granted)
                    .build()
            );

            totalMonthlyLeaves = Math.min(MAX_MONTHLY_LEAVE, totalMonthlyLeaves + granted);
            if (totalMonthlyLeaves >= MAX_MONTHLY_LEAVE) {
                break;
            }

            currentStart = currentEnd.plusDays(1);
        }

        return MonthlyLeaveDetail.builder()
            .records(records)
            .totalLeaveDays(totalMonthlyLeaves)
            .build();
    }


    /**
     * @param accrualPeriods    연차산정기간
     * @param statutoryHolidays 법정공휴일
     * @param companyHolidays   회사휴일
     * @return 연차산정기간의 전체 소정근로일 계산
     */
    public static int getPrescribedWorkingDays(DatePeriod accrualPeriods,
        List<LocalDate> statutoryHolidays, List<LocalDate> companyHolidays) {
        Set<LocalDate> holidays = new HashSet<>(statutoryHolidays);
        holidays.addAll(companyHolidays);
        Set<LocalDate> workingDays = extractDatesWithinPeriod(accrualPeriods);
        return filterPrescribedWorkingDays(workingDays, holidays).size();
    }

    /**
     * @param period 기간
     * @return 기간에 해당하는 날짜들을 추출
     */
    private static Set<LocalDate> extractDatesWithinPeriod(DatePeriod period) {
        return period.startDate().datesUntil(period.endDate().plusDays(1))
            .collect(Collectors.toSet());
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
    // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

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

}
