package com.lawding.leavecalc.flow;


import static com.lawding.leavecalc.constant.AnnualLeaveConstants.MINIMUM_WORK_RATIO;
import static com.lawding.leavecalc.util.AnnualLeaveHelper.*;

import com.lawding.leavecalc.domain.AnnualLeaveContext;
import com.lawding.leavecalc.domain.FlowStep;
import com.lawding.leavecalc.domain.DatePeriod;
import com.lawding.leavecalc.domain.LeaveType;
import com.lawding.leavecalc.domain.flow.FlowResult;
import com.lawding.leavecalc.domain.flow.context.AnnualContext;
import com.lawding.leavecalc.domain.flow.context.CalculationContext;
import com.lawding.leavecalc.domain.flow.context.MonthlyAndProratedContext;
import com.lawding.leavecalc.domain.flow.context.MonthlyContext;
import com.lawding.leavecalc.domain.flow.context.ProratedContext;
import com.lawding.leavecalc.repository.HolidayJdbcRepository;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.ArrayList;
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
        List<LocalDate> companyHolidays = context.getCompanyHolidays();
        Map<Integer, List<DatePeriod>> nonWorkingPeriods = context.getNonWorkingPeriods();
        List<DatePeriod> absentPeriods = nonWorkingPeriods.getOrDefault(2, List.of());
        List<DatePeriod> excludedPeriods = nonWorkingPeriods.getOrDefault(3, List.of());
        LocalDate firstRegularFiscalYearStartDate = calculateFirstRegularFiscalYearStartDate(
            hireDate, fiscalYear); // 첫 정기 회계연도
        List<FlowStep> steps = new ArrayList<>();
        int serviceYears = calculateServiceYears(referenceDate, firstRegularFiscalYearStartDate);
        if (serviceYears >= 3) {
            steps.add(FlowStep.SERVICE_YEARS_3_PLUS);
        }
        steps.add(FlowStep.resolveOneYearStep(hireDate, referenceDate));
        // 1. 첫 정기 회계연도 이전인 경우
        if (isBeforeFirstFiscalYear(referenceDate, firstRegularFiscalYearStartDate)) {
            return handleBeforeFirstFiscalYear(hireDate, referenceDate, fiscalYear, serviceYears,
                companyHolidays, absentPeriods, excludedPeriods, steps);

        }
        return handleAfterFirstFiscalYear(serviceYears, companyHolidays, absentPeriods,
            excludedPeriods, referenceDate, fiscalYear, steps);

    }

    private FlowResult handleBeforeFirstFiscalYear(LocalDate hireDate, LocalDate referenceDate,
        MonthDay fiscalYear, int serviceYears, List<LocalDate> companyHolidays,
        List<DatePeriod> absentPeriods, List<DatePeriod> excludedPeriods, List<FlowStep> steps) {
        // 비례연차 발생일 구하기
        LocalDate proratedLeaveStartDate = getProratedLeaveStartDate(hireDate, fiscalYear);

        if (isBeforeProratedLeaveStartDate(referenceDate, proratedLeaveStartDate)) {
            // 비례연차발생일 이전일 경우, 월차만 발생
            MonthlyContext context = buildMonthlyContextBeforeProrated(hireDate, referenceDate,
                serviceYears,
                companyHolidays, absentPeriods, excludedPeriods);

            return FlowResult.builder()
                .steps(steps)
                .leaveType(LeaveType.MONTHLY)
                .context(context)
                .build();

        } else {
            LocalDate oneYearAnniversary = hireDate.plusYears(1);

            if (!referenceDate.isBefore(oneYearAnniversary)) {
                // 입사 후 1년 이상일 경우, 비례연차
                CalculationContext context = buildProratedContext(hireDate, serviceYears,
                    proratedLeaveStartDate, companyHolidays, absentPeriods, excludedPeriods, steps);

                return FlowResult.builder()
                    .steps(steps)
                    .leaveType(LeaveType.PRORATED)
                    .context(context)
                    .build();

            } else {
                // 월차 + 비례연차가 발생하는 경우
                MonthlyContext monthlyContext = buildMonthlyContextBeforeProrated(hireDate,
                    referenceDate, serviceYears, companyHolidays, absentPeriods, excludedPeriods);

                CalculationContext proratedContext = buildProratedContext(hireDate, serviceYears,
                    proratedLeaveStartDate, companyHolidays, absentPeriods, excludedPeriods, steps);

                MonthlyAndProratedContext context = MonthlyAndProratedContext.builder()
                    .monthlyContext(monthlyContext)
                    .proratedContext(proratedContext)
                    .build();

                return FlowResult.builder()
                    .steps(steps)
                    .leaveType(LeaveType.MONTHLY_AND_PRORATED)
                    .context(context)
                    .build();
            }
        }
    }


    private FlowResult handleAfterFirstFiscalYear(int serviceYears, List<LocalDate> companyHoliday,
        List<DatePeriod> absentPeriods, List<DatePeriod> excludedPeriods, LocalDate referenceDate,
        MonthDay fiscalYear, List<FlowStep> steps) {
        // 연차 산정 기간 구하기
        DatePeriod accrualPeriod = getAccrualPeriodAfterFirstRegularFiscalYearStartDate(
            referenceDate, fiscalYear);
        Set<LocalDate> statutoryHolidays = holidayRepository.findWeekdayHolidays(accrualPeriod);
        int prescribedWorkingDays = countPrescribedWorkingDays(accrualPeriod, statutoryHolidays);
        Set<LocalDate> absentDays = getWorkingDaysInPeriods(accrualPeriod, absentPeriods,
            statutoryHolidays); // 순수 결근처리일
        Set<LocalDate> excludedDays = getWorkingDaysInPeriods(accrualPeriod, excludedPeriods,
            statutoryHolidays); // 순수 소정근로제외일
        Set<LocalDate> companyHolidays = getWorkingDaysInCompanyHolidays(accrualPeriod,
            companyHoliday, statutoryHolidays); // 순수 회사자체휴일
        excludedDays.addAll(companyHolidays);

        double attendanceRate = calculateAttendanceRate(prescribedWorkingDays, absentDays.size(),
            excludedDays.size());

        if (attendanceRate < MINIMUM_WORK_RATIO) {
            steps.add(FlowStep.UNDER_AR);
            // 월차
            MonthlyContext context = MonthlyContext.builder()
                .serviceYears(serviceYears)
                .accrualPeriod(accrualPeriod)
                .absentDays(absentDays)
                .excludedDays(excludedDays)
                .companyHolidays(companyHolidays)
                .statutoryHolidays(statutoryHolidays)
                .attendanceRate(attendanceRate)
                .build();

            return FlowResult.builder()
                .steps(steps)
                .leaveType(LeaveType.MONTHLY)
                .context(context)
                .build();
        }
        steps.add(FlowStep.OVER_AR);

        double prescribeWorkingRatio = calculatePrescribedWorkingRatio(prescribedWorkingDays,
            excludedDays.size());

        if (prescribeWorkingRatio < MINIMUM_WORK_RATIO) {
            steps.add(FlowStep.UNDER_PWR);
        } else {
            steps.add(FlowStep.OVER_PWR);
        }

        AnnualContext context = AnnualContext.builder()
            .serviceYears(serviceYears)
            .accrualPeriod(accrualPeriod)
            .attendanceRate(attendanceRate)
            .prescribeWorkingRatio(prescribeWorkingRatio)
            .build();

        return FlowResult.builder()
            .steps(steps)
            .leaveType(LeaveType.ANNUAL)
            .context(context)
            .build();
    }

    private MonthlyContext buildMonthlyContextBeforeProrated(LocalDate hireDate,
        LocalDate referenceDate,
        int serviceYears, List<LocalDate> companyHoliday, List<DatePeriod> absentPeriods,
        List<DatePeriod> excludedPeriods) {

        DatePeriod accrualPeriod = getAccrualPeriodBeforeFirstRegularFiscalYearStartDate(hireDate,
            referenceDate);

        Set<LocalDate> statutoryHolidays = holidayRepository.findWeekdayHolidays(accrualPeriod);
        Set<LocalDate> absentDays = getWorkingDaysInPeriods(accrualPeriod, absentPeriods,
            statutoryHolidays); // 순수 결근처리일
        Set<LocalDate> excludedDays = getWorkingDaysInPeriods(accrualPeriod, excludedPeriods,
            statutoryHolidays); // 순수 소정근로제외일
        Set<LocalDate> companyHolidays = getWorkingDaysInCompanyHolidays(accrualPeriod,
            companyHoliday, statutoryHolidays); // 순수 회사자체휴일

        return MonthlyContext.builder()
            .serviceYears(serviceYears)
            .accrualPeriod(accrualPeriod)
            .absentDays(absentDays)
            .excludedDays(excludedDays)
            .companyHolidays(companyHolidays)
            .statutoryHolidays(statutoryHolidays)
            .build();
    }

    private CalculationContext buildProratedContext(LocalDate hireDate, int serviceYears,
        LocalDate proratedLeaveStartDate, List<LocalDate> companyHoliday,
        List<DatePeriod> absentPeriods, List<DatePeriod> excludedPeriods, List<FlowStep> steps) {

        DatePeriod accrualPeriod = getAccrualPeriodForProrated(hireDate, proratedLeaveStartDate);

        Set<LocalDate> statutoryHolidays = holidayRepository.findWeekdayHolidays(accrualPeriod);
        int prescribedWorkingDays = countPrescribedWorkingDays(accrualPeriod, statutoryHolidays);
        Set<LocalDate> absentDays = getWorkingDaysInPeriods(accrualPeriod, absentPeriods,
            statutoryHolidays); // 순수 결근처리일
        Set<LocalDate> excludedDays = getWorkingDaysInPeriods(accrualPeriod, excludedPeriods,
            statutoryHolidays); // 순수 소정근로제외일
        Set<LocalDate> companyHolidays = getWorkingDaysInCompanyHolidays(accrualPeriod,
            companyHoliday, statutoryHolidays); // 순수 회사자체휴일
        excludedDays.addAll(companyHolidays);

        double attendanceRate = calculateAttendanceRate(prescribedWorkingDays, absentDays.size(),
            excludedDays.size());

        if (attendanceRate < MINIMUM_WORK_RATIO) {
            steps.add(FlowStep.UNDER_AR);

            return MonthlyContext.builder()
                .serviceYears(serviceYears)
                .accrualPeriod(accrualPeriod)
                .absentDays(absentDays)
                .excludedDays(excludedDays)
                .companyHolidays(companyHolidays)
                .statutoryHolidays(statutoryHolidays)
                .attendanceRate(attendanceRate)
                .build();
        } else {
            steps.add(FlowStep.OVER_AR);

            double prescribeWorkingRatio = calculatePrescribedWorkingRatioForProrated(
                proratedLeaveStartDate, prescribedWorkingDays, excludedDays);

            return ProratedContext.builder()
                .serviceYears(serviceYears)
                .accrualPeriod(accrualPeriod)
                .attendanceRate(attendanceRate)
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

    private double calculatePrescribedWorkingRatioForProrated(LocalDate proratedLeaveStartDate,
        int prescribedWorkingDays, Set<LocalDate> excludedDays) {

        // 직전 회계연도 소정근로일 구하기
        DatePeriod fiscalYearForProrated = getFiscalYearForProrated(proratedLeaveStartDate);
        Set<LocalDate> statutoryHolidays = holidayRepository.findWeekdayHolidays(
            fiscalYearForProrated);
        int denominator = countPrescribedWorkingDays(fiscalYearForProrated, statutoryHolidays);
        if (denominator <= 0) {
            return 0.0;
        }
        return (double) (prescribedWorkingDays - excludedDays.size()) / denominator;
    }


}

