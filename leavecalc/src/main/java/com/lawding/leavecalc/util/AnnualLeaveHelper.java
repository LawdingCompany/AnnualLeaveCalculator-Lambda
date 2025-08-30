package com.lawding.leavecalc.util;

import static com.lawding.leavecalc.constant.AnnualLeaveConstants.*;

import com.lawding.leavecalc.domain.DatePeriod;
import com.lawding.leavecalc.dto.detail.MonthlyLeaveDetail;
import com.lawding.leavecalc.domain.flow.MonthlyCalcContext;
import com.lawding.leavecalc.domain.record.MonthlyLeaveRecord;
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
     * @param absentPeriods     결근처리기간
     * @param standard          기준 기간(시작일, 종료일)
     * @param statutoryHolidays 법정공휴일
     * @param companyHolidays   회사휴일
     * @param excludedPeriods   소정근로제외일
     * @return 결근처리기간들 중 기준 기간 내 소정근로일 계산
     */
    public static int calculatePrescribedWorkingDaysInAbsentPeriods(List<DatePeriod> absentPeriods,
        DatePeriod standard, List<LocalDate> statutoryHolidays, List<LocalDate> companyHolidays,
        List<DatePeriod> excludedPeriods) {
        return getPrescribedWorkingDaySetInAbsentPeriods(absentPeriods, standard,
            statutoryHolidays,
            companyHolidays, excludedPeriods).size();
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
     * @param periods           기간(결근처리 기간 or 소정근로제외 기간)
     * @param companyHolidays   회사 휴일
     * @param statutoryHolidays 법정 공휴일
     * @return 산정 기간 내 결근 기간 중 회사 휴일과 법정 공휴일, 주말을 제외한 결근 일을 담은 Set periods 중 순수한 날을 담은 Set
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

    public static MonthlyLeaveDetail monthlyAccruedLeaves(MonthlyCalcContext calcContext) {
        DatePeriod period = calcContext.getAccrualPeriod();
        Set<LocalDate> absentDays = calcContext.getAbsentDays();
        Set<LocalDate> excludedDays = calcContext.getExcludedDays();

        List<MonthlyLeaveRecord> records = new ArrayList<>();
        int totalMonthlyLeaves = 0;

        LocalDate currentStart = period.startDate();

        while (totalMonthlyLeaves < MAX_MONTHLY_LEAVE) {
            LocalDate currentEnd = currentStart.plusMonths(1).minusDays(1);

            if (currentEnd.isAfter(period.endDate())) {
                break;
            }
            boolean fullAttendance = isFullAttendance(currentStart, currentEnd,
                absentDays, excludedDays);
            double granted = fullAttendance ? 1 : 0;
            records.add(
                MonthlyLeaveRecord.builder()
                    .period(new DatePeriod(currentStart, currentEnd))
                    .monthlyLeave(granted)
                    .build()
            );
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
     * @param startDate    시작일
     * @param endDate      종료일
     * @param absentDays   순수 결근처리일
     * @param excludedDays 순수 소정근로제외일
     * @return [시작일, 종료일]  개근 판단, 기간이 전체 소정근로제외일이면 false(월차 부여x)
     */
    private static boolean isFullAttendance(
        LocalDate startDate, LocalDate endDate,
        Set<LocalDate> absentDays, Set<LocalDate> excludedDays) {

        // null 방어
        Set<LocalDate> abs = (absentDays == null) ? Set.of() : absentDays;
        Set<LocalDate> exc = (excludedDays == null) ? Set.of() : excludedDays;

        boolean hasPureWorkingDay = false;

        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
            boolean isAbsent = abs.contains(d);
            boolean isExcluded = exc.contains(d);

            // 1) 제외로 상쇄되지 않은 결근이 하나라도 있으면 즉시 false
            if (isAbsent && !isExcluded) {
                return false;
            }

            // 2) 결근도 아니고 제외도 아닌 "순수 근무일"이 하나라도 있어야 true 조건 충족
            if (!isAbsent && !isExcluded) {
                hasPureWorkingDay = true;
            }
        }

        // 순수 근무일이 최소 1일 있어야 full attendance로 인정
        return hasPureWorkingDay;
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
}
