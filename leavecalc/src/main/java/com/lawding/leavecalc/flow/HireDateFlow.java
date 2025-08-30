package com.lawding.leavecalc.flow;


import static com.lawding.leavecalc.constant.AnnualLeaveConstants.MINIMUM_WORK_RATIO;
import static com.lawding.leavecalc.util.AnnualLeaveHelper.*;

import com.lawding.leavecalc.domain.AnnualLeaveContext;
import com.lawding.leavecalc.domain.Condition;
import com.lawding.leavecalc.domain.DatePeriod;
import com.lawding.leavecalc.domain.flow.FlowResult;
import com.lawding.leavecalc.domain.LeaveType;
import com.lawding.leavecalc.domain.flow.hiredate.LowPWRFlowResult;
import com.lawding.leavecalc.domain.flow.hiredate.FullFlowResult;
import com.lawding.leavecalc.domain.flow.hiredate.LowARFlowResult;
import com.lawding.leavecalc.domain.flow.hiredate.LessOneYearFlowResult;
import com.lawding.leavecalc.repository.HolidayJdbcRepository;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        List<LocalDate> companyHolidays = context.getCompanyHolidays();
        List<DatePeriod> absentPeriods = nonWorkingPeriods.getOrDefault(2, List.of());
        List<DatePeriod> excludedPeriods = nonWorkingPeriods.getOrDefault(3, List.of());
        int serviceYears = calculateServiceYears(hireDate, referenceDate);

        if (isBeforeOneYearFromHireDate(hireDate, referenceDate)) {
            DatePeriod accrualPeriod = new DatePeriod(hireDate, referenceDate);
            List<LocalDate> holidays = holidayRepository.findWeekdayHolidays(accrualPeriod);
            Set<LocalDate> absentDays = getPrescribedWorkingDayInPeriods(accrualPeriod,
                absentPeriods, companyHolidays, holidays);
            Set<LocalDate> excludedDays = getPrescribedWorkingDayInPeriods(accrualPeriod,
                excludedPeriods, companyHolidays, holidays);
            return LessOneYearFlowResult.builder()
                .leaveType(LeaveType.MONTHLY)
                .condition(Condition.HD_LESS_THAN_ONE_YEAR)
                .accrualPeriod(accrualPeriod)
                .serviceYears(serviceYears)
                .absentDays(absentDays)
                .excludedDays(excludedDays)
                .build();
        }

        // 출근율 계산하기
        DatePeriod accrualPeriod = getAccrualPeriod(hireDate, referenceDate);
        List<LocalDate> holidays = holidayRepository.findWeekdayHolidays(accrualPeriod);
        int prescribedWorkingDays = getPrescribedWorkingDays(accrualPeriod, companyHolidays,
            holidays);
        Set<LocalDate> absentDays = getPrescribedWorkingDayInPeriods(accrualPeriod,
            absentPeriods, companyHolidays, holidays);
        Set<LocalDate> excludedDays = getPrescribedWorkingDayInPeriods(accrualPeriod,
            excludedPeriods, companyHolidays, holidays);
        double attendanceRate = calculateAttendanceRate(prescribedWorkingDays, absentDays.size(),
            excludedDays.size());

        if(attendanceRate<MINIMUM_WORK_RATIO){
            return LowARFlowResult.builder()
                .leaveType(LeaveType.MONTHLY)
                .condition(Condition.HD_LOW_AR)
                .accrualPeriod(accrualPeriod)
                .serviceYears(serviceYears)
                .absentDays(absentDays)
                .excludedDays(excludedDays)
                .attendanceRate(formatDouble(attendanceRate))
                .build();
        }

        double prescribeWorkingRatio = calculatePrescribedWorkingRatio(prescribedWorkingDays,
            excludedDays.size());

        if(prescribeWorkingRatio<MINIMUM_WORK_RATIO){
            return LowPWRFlowResult.builder()
                .leaveType(LeaveType.ANNUAL)
                .condition(Condition.HD_LOW_PWR)
                .accrualPeriod(accrualPeriod)
                .serviceYears(serviceYears)
                .attendanceRate(formatDouble(attendanceRate))
                .prescribeWorkingRatio(formatDouble(prescribeWorkingRatio))
                .build();
        }

        return FullFlowResult.builder()
            .leaveType(LeaveType.ANNUAL)
            .condition(Condition.HD_FULL)
            .accrualPeriod(accrualPeriod)
            .serviceYears(serviceYears)
            .attendanceRate(formatDouble(attendanceRate))
            .prescribeWorkingRatio(formatDouble(prescribeWorkingRatio))
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
    private DatePeriod getAccrualPeriod(LocalDate hireDate,
        LocalDate referenceDate) {
        int years = calculateServiceYears(hireDate, referenceDate) - 1;
        LocalDate start = hireDate.plusYears(years);
        LocalDate end = start.plusYears(1).minusDays(1);
        return new DatePeriod(start, end);
    }


}

