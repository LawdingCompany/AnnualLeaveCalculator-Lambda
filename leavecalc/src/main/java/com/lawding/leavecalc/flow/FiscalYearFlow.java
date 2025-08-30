package com.lawding.leavecalc.flow;


import static com.lawding.leavecalc.constant.AnnualLeaveConstants.MINIMUM_WORK_RATIO;
import static com.lawding.leavecalc.util.AnnualLeaveHelper.*;

import com.lawding.leavecalc.domain.AnnualLeaveContext;
import com.lawding.leavecalc.domain.Condition;
import com.lawding.leavecalc.domain.DatePeriod;
import com.lawding.leavecalc.domain.LeaveType;
import com.lawding.leavecalc.domain.flow.FlowResult;
import com.lawding.leavecalc.domain.flow.hiredate.FullFlowResult;
import com.lawding.leavecalc.domain.flow.UnderARFlowResult;
import com.lawding.leavecalc.domain.flow.UnderPWRFlowResult;
import com.lawding.leavecalc.domain.flow.hiredate.LessOneYearFlowResult;
import com.lawding.leavecalc.repository.HolidayJdbcRepository;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FiscalYearFlow implements CalculationFlow {

    private final HolidayJdbcRepository holidayRepository;

    public FiscalYearFlow(HolidayJdbcRepository holidayRepository) {
        this.holidayRepository = holidayRepository;
    }

    @Override
    public FlowResult process(AnnualLeaveContext context) {
        LocalDate hireDate = context.getHireDate();
        LocalDate referenceDate = context.getReferenceDate();
        MonthDay fiscalYear = context.getFiscalYear();
        LocalDate firstRegularFiscalYearStartDate = calculateFirstRegularFiscalYearStartDate(
            hireDate, fiscalYear); // 첫 정기 회계연도
        List<LocalDate> companyHolidays = context.getCompanyHolidays();
        Map<Integer, List<DatePeriod>> nonWorkingPeriods = context.getNonWorkingPeriods();
        List<DatePeriod> absentPeriods = nonWorkingPeriods.getOrDefault(2, List.of());
        List<DatePeriod> excludedPeriods = nonWorkingPeriods.getOrDefault(3, List.of());
        int serviceYears = calculateServiceYears(referenceDate,
            firstRegularFiscalYearStartDate);
        if (isBeforeFirstFiscalYear(referenceDate, firstRegularFiscalYearStartDate)) {
            // 월차 계산 준비
            DatePeriod accrualPeriodForMonthly = getAccrualPeriodBeforeFirstRegularFiscalYearStartDate(
                hireDate, referenceDate);
            List<LocalDate> holidaysWithinMonthly = holidayRepository.findWeekdayHolidays(
                accrualPeriodForMonthly);
            Set<LocalDate> absentDaysWithinMonthly = getPrescribedWorkingDayInPeriods(
                accrualPeriodForMonthly,
                absentPeriods, companyHolidays, holidaysWithinMonthly);
            Set<LocalDate> excludedDaysWithinMonthly = getPrescribedWorkingDayInPeriods(
                accrualPeriodForMonthly,
                excludedPeriods, companyHolidays, holidaysWithinMonthly);

            // 비례연차 부여날짜 구하기
            LocalDate proratedLeaveStartDate = getProratedLeaveStartDate(hireDate, fiscalYear);
            // 산정일이 비례연차부여일 이전일 경우, 월차만 발생
            if (isBeforeProratedLeaveStartDate(referenceDate, proratedLeaveStartDate)) {
                return LessOneYearFlowResult.builder()
                    .leaveType(LeaveType.MONTHLY)
                    .condition(
                        Condition.FY_BEFORE_FIRST_REGULAR_START_DATE_AND_BEFORE_PRORATED_START_DATE)
                    .accrualPeriod(accrualPeriodForMonthly)
                    .serviceYears(serviceYears)
                    .absentDays(absentDaysWithinMonthly)
                    .excludedDays(excludedDaysWithinMonthly)
                    .build();
            }
            // 비례연차 계산, 입사일 ~ 회계연도 종료일에 비례해서
            DatePeriod accrualPeriodForProrated = getAccrualPeriodForProrated(hireDate,
                proratedLeaveStartDate);
            List<LocalDate> holidaysWithinProrated = holidayRepository.findWeekdayHolidays(
                accrualPeriodForProrated);
            int prescribedWorkingDays = calculatePrescribedWorkingDays(accrualPeriodForProrated,
                companyHolidays, holidaysWithinProrated);
            Set<LocalDate> absentDaysWithinProrated = getPrescribedWorkingDayInPeriods(
                accrualPeriodForProrated,
                absentPeriods, companyHolidays, holidaysWithinProrated);
            Set<LocalDate> excludedDaysWithinProrated = getPrescribedWorkingDayInPeriods(
                accrualPeriodForProrated,
                excludedPeriods, companyHolidays, holidaysWithinProrated);
            double attendanceRate = calculateAttendanceRate(prescribedWorkingDays,
                absentDaysWithinProrated.size(), excludedDaysWithinProrated.size());
            if (attendanceRate < MINIMUM_WORK_RATIO) {
                return UnderARFlowResult.builder()
                    .leaveType(LeaveType.MONTHLY)
                    .condition(Condition.HD_AFTER_ONE_YEAR_AND_UNDER_AR)
                    .accrualPeriod(accrualPeriod)
                    .serviceYears(serviceYears)
                    .absentDays(absentDaysWithinProrated)
                    .excludedDays(excludedDaysWithinProrated)
                    .attendanceRate(formatDouble(attendanceRate))
                    .build();
            }
            // 산정일이 입사일보다 1년 이전일 경우,
//            DatePeriod accrualPeriod = new DatePeriod(hireDate, referenceDate);
//            return FlowResult.builder()
//                .leaveType(LeaveType.MONTHY)
//                .accrualPeriod(accrualPeriod)
//                .build();
        }
        DatePeriod accrualPeriod = getAccrualPeriodAfterFirstRegularFiscalYearStartDate(
            referenceDate, fiscalYear);
        List<LocalDate> holidays = holidayRepository.findWeekdayHolidays(
            accrualPeriod);
        int prescribedWorkingDays = getPrescribedWorkingDays(accrualPeriod, companyHolidays,
            holidays);
        Set<LocalDate> absentDays = getPrescribedWorkingDayInPeriods(accrualPeriod,
            absentPeriods, companyHolidays, holidays);
        Set<LocalDate> excludedDays = getPrescribedWorkingDayInPeriods(accrualPeriod,
            excludedPeriods, companyHolidays, holidays);
        double attendanceRate = calculateAttendanceRate(prescribedWorkingDays, absentDays.size(),
            excludedDays.size());

        if (attendanceRate < MINIMUM_WORK_RATIO) {
            return UnderARFlowResult.builder()
                .leaveType(LeaveType.MONTHLY)
                .condition(Condition.FY_AFTER_FIRST_REGULAR_START_DATE_AND_UNDER_AR)
                .accrualPeriod(accrualPeriod)
                .serviceYears(serviceYears)
                .absentDays(absentDays)
                .excludedDays(excludedDays)
                .attendanceRate(formatDouble(attendanceRate))
                .build();
        }

        double prescribeWorkingRatio = calculatePrescribedWorkingRatio(prescribedWorkingDays,
            excludedDays.size());

        if (prescribeWorkingRatio < MINIMUM_WORK_RATIO) {
            return UnderPWRFlowResult.builder()
                .leaveType(LeaveType.ANNUAL)
                .condition(Condition.FY_AFTER_FIRST_REGULAR_START_DATE_AND_UNDER_AR)
                .accrualPeriod(accrualPeriod)
                .serviceYears(serviceYears)
                .attendanceRate(formatDouble(attendanceRate))
                .prescribeWorkingRatio(formatDouble(prescribeWorkingRatio))
                .build();
        }

        return FullFlowResult.builder()
            .leaveType(LeaveType.ANNUAL)
            .condition(Condition.FY_FULL)
            .accrualPeriod(accrualPeriod)
            .serviceYears(serviceYears)
            .attendanceRate(formatDouble(attendanceRate))
            .prescribeWorkingRatio(formatDouble(prescribeWorkingRatio))
            .build();
    }

    private boolean isBeforeFirstFiscalYear(LocalDate referenceDate,
        LocalDate firstRegularFiscalYearStartDate) {
        return referenceDate.isBefore(firstRegularFiscalYearStartDate);
    }

    private boolean isBeforeProratedLeaveStartDate(LocalDate referenceDate,
        LocalDate proratedLeaveStartDate) {
        return referenceDate.isBefore(proratedLeaveStartDate);
    }

    @Override
    public int calculateServiceYears(LocalDate referenceDate,
        LocalDate firstRegularFiscalYearStartDate) {
        if (referenceDate.isBefore(firstRegularFiscalYearStartDate)) {
            return 0;
        }
        return referenceDate.getYear() - firstRegularFiscalYearStartDate.getYear() + 1;
    }

    /**
     * 입사 1주년 이후 처음으로 정규 연차가 발생하는 회계연도 시작일을 계산하는 함수
     * <p>
     * 예시) 회계연도 = 01-01 <p> 입사일 = 2024-01-01인 경우, 다음 회계연도 시작일 = 2025-01-01 <p>입사일 = 2024-03-02인 경우,
     * 다음 회계연도 시작일 = 2026-01-01
     *
     * @param hireDate   입사일
     * @param fiscalYear 회계연도 시작일("MM-dd")
     * @return 정규 연차가 처음 적용되는 회계연도 시작일 첫 회계연도 정규 연차 발생일 입사일 = 회계연도 시작일 => 다음 해 회계연도 시작일 입사일 != 회계연도
     * 시작일 => 다다음해 회계연도 시작일 (> <)
     * <p>
     * 입사 1주년 구해 1주년에 해당하는 회계연도를 구해 1/1 = !=
     * <p>
     * <- 6/1 -> < = > 입사 1주년  > 회계연도시작일 =>
     */
    private static LocalDate calculateFirstRegularFiscalYearStartDate(LocalDate
        hireDate, MonthDay fiscalYear) {
        LocalDate oneYearAnniversaryDate = hireDate.plusYears(1); // 2025-03-02
        LocalDate nextFiscalYear = fiscalYear.atYear( // 2025-06-01
            oneYearAnniversaryDate.getYear());
        if (oneYearAnniversaryDate.isAfter(
            nextFiscalYear)) { // 2025-03-02, 2025-06-01  < 2025-06-01 => 첫 회계연도 정규 연차 발생일 = 2025-06-01  // 입사1주년 : 2025-07-01 >  2025-06-01 => 2026-06-01
            // 입사 1주년 > 회계연도 시작일 => 다음해 회계연도 시작일
            nextFiscalYear = nextFiscalYear.plusYears(1);
        }
        return nextFiscalYear;
    }

    /**
     * @param referenceDate 산정 기준일
     * @param fiscalYear    회계연도 시작일
     * @return 정규 회계연도 시작 이후 연차 산정기간 계산 방법
     */
    private DatePeriod getAccrualPeriodAfterFirstRegularFiscalYearStartDate(LocalDate referenceDate,
        MonthDay fiscalYear) {
        LocalDate prevFiscalYearStartDate = fiscalYear.atYear(referenceDate.getYear());
        if (prevFiscalYearStartDate.isAfter(referenceDate)) {
            prevFiscalYearStartDate = prevFiscalYearStartDate.minusYears(2);
        } else {
            prevFiscalYearStartDate = prevFiscalYearStartDate.minusYears(1);
        }
        LocalDate prevFiscalYearEndDate = prevFiscalYearStartDate.plusYears(1).minusDays(1);
        return new DatePeriod(prevFiscalYearStartDate, prevFiscalYearEndDate);
    }

    private DatePeriod getAccrualPeriodBeforeFirstRegularFiscalYearStartDate(
        LocalDate hireDate,
        LocalDate referenceDate) {

        LocalDate oneYearMinus1 = hireDate.plusYears(1).minusDays(1);

        LocalDate accrualPeriodEndDate =
            referenceDate.isBefore(oneYearMinus1) ? referenceDate : oneYearMinus1;

        return new DatePeriod(hireDate, accrualPeriodEndDate);
    }


    /**
     * @param hireDate   입사일
     * @param fiscalYear 회계연도
     * @return 비례연차 부여일 = 입사 후 다음 회계연도 시작일
     */
    private LocalDate getProratedLeaveStartDate(LocalDate hireDate, MonthDay fiscalYear) {
        LocalDate fiscalStart = fiscalYear.atYear(hireDate.getYear());

        return hireDate.isBefore(fiscalStart) ? fiscalStart : fiscalStart.plusYears(1);
    }

    private DatePeriod getAccrualPeriodForProrated(LocalDate hireDate,
        LocalDate proratedLeaveStartDate) {
        return new DatePeriod(hireDate, proratedLeaveStartDate.minusDays(1));
    }

}

