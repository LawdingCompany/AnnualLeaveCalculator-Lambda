package com.lawding.leavecalc.flow;


import static com.lawding.leavecalc.constant.AnnualLeaveConstants.MINIMUM_WORK_RATIO;
import static com.lawding.leavecalc.util.AnnualLeaveHelper.*;

import com.lawding.leavecalc.domain.AnnualLeaveContext;
import com.lawding.leavecalc.domain.FlowStep;
import com.lawding.leavecalc.domain.DatePeriod;
import com.lawding.leavecalc.domain.LeaveType;
import com.lawding.leavecalc.domain.flow.FlowResult;
import com.lawding.leavecalc.domain.flow.fiscalyear.BeforeProratedFlowResult;
import com.lawding.leavecalc.domain.flow.fiscalyear.FullProratedFlowResult;
import com.lawding.leavecalc.domain.flow.fiscalyear.MonthlyAndProratedFlowResult;
import com.lawding.leavecalc.domain.flow.fiscalyear.ProratedUnderARFlowResult;
import com.lawding.leavecalc.domain.flow.hiredate.FullFlowResult;
import com.lawding.leavecalc.domain.flow.hiredate.UnderARFlowResult;
import com.lawding.leavecalc.domain.flow.hiredate.UnderPWRFlowResult;
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
        int serviceYears = calculateServiceYears(referenceDate, firstRegularFiscalYearStartDate);

        // 1. 첫 정기 회계연도 이전인 경우
        if (isBeforeFirstFiscalYear(referenceDate, firstRegularFiscalYearStartDate)) {
            return handleBeforeFirstFiscalYear(hireDate, referenceDate, fiscalYear, serviceYears,
                companyHolidays, absentPeriods, excludedPeriods);

        }
        return handleAfterFirstFiscalYear(serviceYears, companyHolidays, absentPeriods,
            excludedPeriods, referenceDate, fiscalYear);

    }

    private FlowResult handleBeforeFirstFiscalYear(LocalDate hireDate, LocalDate referenceDate,
        MonthDay fiscalYear, int serviceYears, List<LocalDate> companyHolidays,
        List<DatePeriod> absentPeriods, List<DatePeriod> excludedPeriods) {
        // 비례연차 발생일 구하기
        LocalDate proratedLeaveStartDate = getProratedLeaveStartDate(hireDate, fiscalYear);

        if (isBeforeProratedLeaveStartDate(referenceDate, proratedLeaveStartDate)) {
            // 비례연차발생일 이전일 경우, 월차만 발생
            return buildMonthlyFlowResult(hireDate, referenceDate, serviceYears,
                companyHolidays, absentPeriods, excludedPeriods);

        } else {
            LocalDate oneYearAnniversary = hireDate.plusYears(1);

            if (!referenceDate.isBefore(oneYearAnniversary)) {
                // 입사 후 1년 이상일 경우, 비례연차
                return buildProratedFlowResult(hireDate, serviceYears, proratedLeaveStartDate,
                    companyHolidays, absentPeriods, excludedPeriods);
            } else {
                // 월차 + 비례연차가 발생하는 경우
                FlowResult monthlyPart = buildMonthlyFlowResult(
                    hireDate, referenceDate, serviceYears,
                    companyHolidays, absentPeriods, excludedPeriods
                );

                FlowResult proratedPart = buildProratedFlowResult(
                    hireDate, serviceYears, proratedLeaveStartDate,
                    companyHolidays, absentPeriods, excludedPeriods
                );

                return MonthlyAndProratedFlowResult.builder()
                    .leaveType(LeaveType.MONTHLY_AND_PRORATED)
                    .flowStep(FlowStep.FY_MONTHLY_AND_PRORATED)
                    .monthlyPart((BeforeProratedFlowResult) monthlyPart)
                    .proratedPart(proratedPart)
                    .build();
            }
        }
    }


    private FlowResult handleAfterFirstFiscalYear(int serviceYears, List<LocalDate> companyHolidays,
        List<DatePeriod> absentPeriods, List<DatePeriod> excludedPeriods, LocalDate referenceDate,
        MonthDay fiscalYear) {
        // 연차 산정 기간 구하기
        DatePeriod accrualPeriod = getAccrualPeriodAfterFirstRegularFiscalYearStartDate(
            referenceDate, fiscalYear);
        List<LocalDate> holidays = holidayRepository.findWeekdayHolidays(accrualPeriod);
        int prescribedWorkingDays = getPrescribedWorkingDays(accrualPeriod, companyHolidays,
            holidays);
        Set<LocalDate> absentDays = getPrescribedWorkingDayInPeriods(accrualPeriod, absentPeriods,
            companyHolidays, holidays);
        Set<LocalDate> excludedDays = getPrescribedWorkingDayInPeriods(accrualPeriod,
            excludedPeriods, companyHolidays, holidays);

        double attendanceRate = calculateAttendanceRate(prescribedWorkingDays, absentDays.size(),
            excludedDays.size());
        
        if (attendanceRate < MINIMUM_WORK_RATIO) {
            // 월차
            Set<LocalDate> totalHolidays = getPrescribedWorkingDayInHolidays(accrualPeriod,
                companyHolidays, holidays);
            return UnderARFlowResult.builder()
                .leaveType(LeaveType.MONTHLY)
                .flowStep(FlowStep.FY_AFTER_FIRST_REGULAR_START_DATE_AND_UNDER_AR)
                .accrualPeriod(accrualPeriod)
                .serviceYears(serviceYears)
                .absentDays(absentDays)
                .excludedDays(excludedDays)
                .holidays(totalHolidays)
                .attendanceRate(formatDouble(attendanceRate))
                .build();
        }

        double prescribeWorkingRatio = calculatePrescribedWorkingRatio(prescribedWorkingDays,
            excludedDays.size());

        if (prescribeWorkingRatio < MINIMUM_WORK_RATIO) {
            return UnderPWRFlowResult.builder()
                .leaveType(LeaveType.ANNUAL)
                .flowStep(FlowStep.FY_AFTER_FIRST_REGULAR_START_DATE_AND_UNDER_AR)
                .accrualPeriod(accrualPeriod)
                .serviceYears(serviceYears)
                .attendanceRate(formatDouble(attendanceRate))
                .prescribeWorkingRatio(formatDouble(prescribeWorkingRatio))
                .build();
        }

        return FullFlowResult.builder()
            .leaveType(LeaveType.ANNUAL)
            .flowStep(FlowStep.FY_FULL)
            .accrualPeriod(accrualPeriod)
            .serviceYears(serviceYears)
            .attendanceRate(formatDouble(attendanceRate))
            .prescribeWorkingRatio(formatDouble(prescribeWorkingRatio))
            .build();
    }

    private FlowResult buildMonthlyFlowResult(
        LocalDate hireDate,
        LocalDate referenceDate,
        int serviceYears,
        List<LocalDate> companyHolidays,
        List<DatePeriod> absentPeriods,
        List<DatePeriod> excludedPeriods
    ) {
        DatePeriod accrualPeriodForMonthly = getAccrualPeriodBeforeFirstRegularFiscalYearStartDate(
            hireDate, referenceDate);
        List<LocalDate> holidaysWithinMonthly = holidayRepository.findWeekdayHolidays(
            accrualPeriodForMonthly);
        Set<LocalDate> absentDaysWithinMonthly = getPrescribedWorkingDayInPeriods( // 순수 결근일
            accrualPeriodForMonthly, absentPeriods, companyHolidays, holidaysWithinMonthly);
        Set<LocalDate> excludedDaysWithinMonthly = getPrescribedWorkingDayInPeriods(
            accrualPeriodForMonthly, excludedPeriods, companyHolidays, holidaysWithinMonthly);
        Set<LocalDate> totalHolidaysWithinMonthly = getPrescribedWorkingDayInHolidays(
            accrualPeriodForMonthly, companyHolidays, holidaysWithinMonthly);

        return BeforeProratedFlowResult.builder()
            .leaveType(LeaveType.MONTHLY)
            .flowStep(FlowStep.FY_BEFORE_PRORATED)
            .accrualPeriod(accrualPeriodForMonthly)
            .serviceYears(serviceYears)
            .absentDays(absentDaysWithinMonthly)
            .excludedDays(excludedDaysWithinMonthly)
            .holidays(totalHolidaysWithinMonthly)
            .build();
    }

    private FlowResult buildProratedFlowResult(LocalDate hireDate,
        int serviceYears,
        LocalDate proratedLeaveStartDate,
        List<LocalDate> companyHolidays,
        List<DatePeriod> absentPeriods,
        List<DatePeriod> excludedPeriods) {
        DatePeriod accrualPeriodForProrated = getAccrualPeriodForProrated(hireDate,
            proratedLeaveStartDate);

        List<LocalDate> holidaysWithinProrated = holidayRepository.findWeekdayHolidays(
            accrualPeriodForProrated);
        int prescribedWorkingDaysWithinAccrualPeriodForProrated = getPrescribedWorkingDays(
            accrualPeriodForProrated, companyHolidays, holidaysWithinProrated);
        Set<LocalDate> absentDaysWithinProrated = getPrescribedWorkingDayInPeriods(
            accrualPeriodForProrated, absentPeriods, companyHolidays, holidaysWithinProrated);
        Set<LocalDate> excludedDaysWithinProrated = getPrescribedWorkingDayInPeriods(
            accrualPeriodForProrated, excludedPeriods, companyHolidays, holidaysWithinProrated);
        double attendanceRate = calculateAttendanceRate(
            prescribedWorkingDaysWithinAccrualPeriodForProrated,
            absentDaysWithinProrated.size(), excludedDaysWithinProrated.size());

        if (attendanceRate < MINIMUM_WORK_RATIO) {
            Set<LocalDate> totalHolidaysWithinProrated = getPrescribedWorkingDayInHolidays(
                accrualPeriodForProrated,
                companyHolidays, holidaysWithinProrated);
            return ProratedUnderARFlowResult.builder()
                .leaveType(LeaveType.PRORATED)
                .flowStep(FlowStep.FY_PRORATED_AND_UNDER_AR)
                .accrualPeriod(accrualPeriodForProrated)
                .serviceYears(serviceYears)
                .absentDays(absentDaysWithinProrated)
                .excludedDays(excludedDaysWithinProrated)
                .attendanceRate(formatDouble(attendanceRate))
                .holidays(totalHolidaysWithinProrated)
                .build();
        } else {
            DatePeriod fiscalYearForProrated = getFiscalYearForProrated(proratedLeaveStartDate);
            List<LocalDate> holidaysWithinFiscalYear = holidayRepository.findWeekdayHolidays(
                fiscalYearForProrated);
            int prescribedWorkingDaysWithinFiscalYear = getPrescribedWorkingDays(
                fiscalYearForProrated, companyHolidays,
                holidaysWithinFiscalYear);
            double prescribeWorkingRatio = calculatePrescribedWorkingRatioForProrated(
                prescribedWorkingDaysWithinAccrualPeriodForProrated,
                excludedDaysWithinProrated.size(),
                prescribedWorkingDaysWithinFiscalYear);
            return FullProratedFlowResult.builder()
                .leaveType(LeaveType.PRORATED)
                .flowStep(FlowStep.FY_PRORATED_FULL)
                .accrualPeriod(accrualPeriodForProrated)
                .serviceYears(serviceYears)
                .attendanceRate(formatDouble(attendanceRate))
                .prescribeWorkingRatio(prescribeWorkingRatio)
                .build();
        }
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

    private DatePeriod getFiscalYearForProrated(LocalDate proratedLeaveStartDate) {
        return new DatePeriod(proratedLeaveStartDate.minusYears(1),
            proratedLeaveStartDate.minusDays(1));
    }

    private double calculatePrescribedWorkingRatioForProrated(int numerator1, int numerator2,
        int denominator) {
        if (denominator <= 0 || numerator1 <= numerator2) {
            return 0.0;
        }
        return (double) (numerator1 - numerator2) / denominator;
    }

}

