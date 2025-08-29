package com.lawding.leavecalc.flow;


import com.lawding.leavecalc.domain.AnnualLeaveContext;
import com.lawding.leavecalc.domain.DatePeriod;
import com.lawding.leavecalc.domain.flow.FlowResult;
import com.lawding.leavecalc.domain.LeaveType;
import com.lawding.leavecalc.domain.flow.hiredate.LessOneYearFlowResult;
import com.lawding.leavecalc.repository.HolidayJdbcRepository;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Map;

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
        if(isBeforeOneYearFromHireDate(hireDate, referenceDate)) {
            DatePeriod accrualPeriod = new DatePeriod(hireDate, referenceDate);
            return LessOneYearFlowResult.builder()
                .leaveType(LeaveType.MONTHY)
                .accrualPeriod(accrualPeriod)
                .build();
        }
        // 출근율 계산하기
        // accrualPeriod holidays companyHolidays excludedPeriods
        double attendanceRate = calculateAttendanceRate()
        else if(isLowAttendance())
        Map<Integer, List<DatePeriod>> nonWorkingPeriods = annualLeaveContext.getNonWorkingPeriods();
        List<LocalDate> companyHolidays = annualLeaveContext.getCompanyHolidays();
        List<DatePeriod> absentPeriods = nonWorkingPeriods.getOrDefault(2, List.of());
        List<DatePeriod> excludedPeriods = nonWorkingPeriods.getOrDefault(3, List.of());
        return null;
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
}

