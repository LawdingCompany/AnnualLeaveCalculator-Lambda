package com.lawding.leavecalc.flow;


import static com.lawding.leavecalc.constant.AnnualLeaveConstants.MINIMUM_WORK_RATIO;
import static com.lawding.leavecalc.util.AnnualLeaveHelper.*;
import static com.lawding.leavecalc.util.AnnualLeaveHelper.getWorkingDaysInPeriods;

import com.lawding.leavecalc.domain.AnnualLeaveContext;
import com.lawding.leavecalc.domain.FlowStep;
import com.lawding.leavecalc.domain.DatePeriod;
import com.lawding.leavecalc.domain.flow.FlowResult;
import com.lawding.leavecalc.domain.LeaveType;
import com.lawding.leavecalc.domain.flow.context.AnnualContext;
import com.lawding.leavecalc.domain.flow.context.MonthlyContext;
import com.lawding.leavecalc.repository.HolidayJdbcRepository;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Flow;

public class HireDateFlow implements CalculationFlow {

    private final HolidayJdbcRepository holidayRepository;

    public HireDateFlow(HolidayJdbcRepository holidayRepository) {
        this.holidayRepository = holidayRepository;
    }

    @Override
    public FlowResult process(AnnualLeaveContext context) {
        LocalDate hireDate = context.getHireDate();
        LocalDate referenceDate = context.getReferenceDate();
        Map<Integer, List<DatePeriod>> nonWorkingPeriods = context.getNonWorkingPeriods();
        List<LocalDate> companyHoliday = context.getCompanyHolidays();
        List<DatePeriod> absentPeriods = nonWorkingPeriods.getOrDefault(2, List.of());
        List<DatePeriod> excludedPeriods = nonWorkingPeriods.getOrDefault(3, List.of());
        List<FlowStep> steps = new ArrayList<>();
        int serviceYears = calculateServiceYears(hireDate, referenceDate);
        if (serviceYears >= 3) {
            steps.add(FlowStep.SERVICE_YEARS_3_PLUS);
        }
        steps.add(FlowStep.resolveOneYearStep(hireDate, referenceDate));
        if (isBeforeOneYearFromHireDate(hireDate, referenceDate)) {
            return handleBeforeOneYear(hireDate, referenceDate, serviceYears,
                companyHoliday, absentPeriods, excludedPeriods, steps);
        }
        return handleAfterOneYear(hireDate, referenceDate, serviceYears,
            companyHoliday, absentPeriods, excludedPeriods, steps);
    }

    private FlowResult handleBeforeOneYear(LocalDate hireDate, LocalDate referenceDate,
        int serviceYears, List<LocalDate> companyHoliday, List<DatePeriod> absentPeriods,
        List<DatePeriod> excludedPeriods, List<FlowStep> steps) {

        DatePeriod accrualPeriod = new DatePeriod(hireDate, referenceDate);
        DatePeriod availablePeriod = getAvailablePeriod(hireDate, referenceDate);

        Set<LocalDate> statutoryHolidays = holidayRepository.findWeekdayHolidays(
            accrualPeriod); // 산정기간 내 법정 공휴일
        Set<LocalDate> absentDays = getWorkingDaysInPeriods(accrualPeriod, absentPeriods,
            statutoryHolidays); // 순수 결근처리일
        Set<LocalDate> excludedDays = getWorkingDaysInPeriods(accrualPeriod, excludedPeriods,
            statutoryHolidays); // 순수 소정근로제외일
        Set<LocalDate> companyHolidays = getWorkingDaysInCompanyHolidays(accrualPeriod,
            companyHoliday, statutoryHolidays); // 순수 회사자체휴일

        MonthlyContext context = MonthlyContext.builder()
            .serviceYears(serviceYears)
            .accrualPeriod(accrualPeriod)
            .availablePeriod(availablePeriod)
            .absentDays(absentDays)
            .excludedDays(excludedDays)
            .companyHolidays(companyHolidays)
            .statutoryHolidays(statutoryHolidays)
            .build();

        return FlowResult.builder()
            .steps(steps)
            .leaveType(LeaveType.MONTHLY)
            .context(context)
            .build();
    }

    private FlowResult handleAfterOneYear(LocalDate hireDate, LocalDate referenceDate,
        int serviceYears, List<LocalDate> companyHoliday, List<DatePeriod> absentPeriods,
        List<DatePeriod> excludedPeriods, List<FlowStep> steps) {

        DatePeriod accrualPeriod = getAccrualPeriod(hireDate, referenceDate);
        DatePeriod availablePeriod = getAvailablePeriod(hireDate, referenceDate);

        Set<LocalDate> statutoryHolidays = holidayRepository.findWeekdayHolidays(accrualPeriod);
        int prescribedWorkingDays = countPrescribedWorkingDays(accrualPeriod, statutoryHolidays);
        Set<LocalDate> absentDays = getWorkingDaysInPeriods(accrualPeriod, absentPeriods,
            statutoryHolidays); // 순수 결근처리일
        Set<LocalDate> excludedDays = new HashSet<>(
            getWorkingDaysInPeriods(accrualPeriod, excludedPeriods,
                statutoryHolidays)); // 순수 소정근로제외일
        Set<LocalDate> companyHolidays = new HashSet<>(
            getWorkingDaysInCompanyHolidays(accrualPeriod,
                companyHoliday, statutoryHolidays)); // 순수 회사자체휴일
        excludedDays.addAll(companyHolidays);

        double attendanceRate = calculateAttendanceRate(prescribedWorkingDays, absentDays.size(),
            excludedDays.size());
        double prescribeWorkingRatio = calculatePrescribedWorkingRatio(prescribedWorkingDays,
            excludedDays.size());

        if (attendanceRate < MINIMUM_WORK_RATIO) {
            steps.add(FlowStep.UNDER_AR);
            steps.add(FlowStep.stepPWR(prescribeWorkingRatio));

            MonthlyContext context = MonthlyContext.builder()
                .serviceYears(serviceYears)
                .accrualPeriod(accrualPeriod)
                .availablePeriod(availablePeriod)
                .absentDays(absentDays)
                .excludedDays(excludedDays)
                .companyHolidays(companyHolidays)
                .statutoryHolidays(statutoryHolidays)
                .attendanceRate(formatDouble(attendanceRate))
                .prescribedWorkingRatio(formatDouble(prescribeWorkingRatio))
                .build();

            return FlowResult.builder()
                .steps(steps)
                .leaveType(LeaveType.MONTHLY)
                .context(context)
                .build();

        }
        steps.add(FlowStep.OVER_AR);
        steps.add(FlowStep.stepPWR(prescribeWorkingRatio));

        AnnualContext context = AnnualContext.builder()
            .serviceYears(serviceYears)
            .accrualPeriod(accrualPeriod)
            .availablePeriod(availablePeriod)
            .attendanceRate(formatDouble(attendanceRate))
            .prescribedWorkingRatio(formatDouble(prescribeWorkingRatio))
            .build();

        return FlowResult.builder()
            .steps(steps)
            .leaveType(LeaveType.ANNUAL)
            .context(context)
            .build();
    }

    /**
     * 근속연수를 계산하는 함수
     *
     * @param startDate 시작일
     * @param endDate   종료일
     * @return 근속연수
     */
    @Override
    public int calculateServiceYears(LocalDate startDate, LocalDate endDate) {
        return Period.between(startDate, endDate).getYears();
    }

    /**
     * 산정일 기준 직전 연차 산정 기간을 계산하는 함수
     *
     * @param hireDate      입사일
     * @param referenceDate 기준일
     * @return 연차 산정 단위 기간 [시작일, 종료일]
     */
    private DatePeriod getAccrualPeriod(LocalDate hireDate, LocalDate referenceDate) {
        int years = calculateServiceYears(hireDate, referenceDate) - 1;
        LocalDate start = hireDate.plusYears(years);
        LocalDate end = start.plusYears(1).minusDays(1);
        return new DatePeriod(start, end);
    }

    private DatePeriod getAvailablePeriod(LocalDate hireDate, LocalDate referenceDate) {
        LocalDate hireDateThisYear = LocalDate.of(
            referenceDate.getYear(),
            hireDate.getMonth(),
            hireDate.getDayOfMonth()
        );

        LocalDate nextHireDate;
        if (referenceDate.isBefore(hireDateThisYear)) {
            nextHireDate = hireDateThisYear;
        } else {
            nextHireDate = hireDateThisYear.plusYears(1);
        }

        LocalDate endDate = nextHireDate.minusDays(1);
        return new DatePeriod(referenceDate, endDate);
    }
}

