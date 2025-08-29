package com.lawding.leavecalc.flow;


import com.lawding.leavecalc.domain.AnnualLeaveContext;
import com.lawding.leavecalc.domain.flow.FlowResult;
import com.lawding.leavecalc.repository.HolidayJdbcRepository;
import java.time.LocalDate;

public class FiscalYearFlow implements CalculationFlow {

    private final HolidayJdbcRepository holidayRepository;

    public FiscalYearFlow(HolidayJdbcRepository holidayRepository) {
        this.holidayRepository = holidayRepository;
    }

    @Override
    public FlowResult process(AnnualLeaveContext context) {
        LocalDate hireDate = context.getHireDate();
        LocalDate referenceDate = context.getReferenceDate();
        if(isBeforeOneYearFromHireDate(hireDate, referenceDate)) {
//            DatePeriod accrualPeriod = new DatePeriod(hireDate, referenceDate);
//            return FlowResult.builder()
//                .leaveType(LeaveType.MONTHY)
//                .accrualPeriod(accrualPeriod)
//                .build();
        }
        // 출근율 계산하기
        return null;
    }

    @Override
    public int calculateServiceYears(LocalDate startDate, LocalDate endDate) {
        return 0;
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

}

