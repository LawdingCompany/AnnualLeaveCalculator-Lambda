package com.lawding.leavecalc.flow;


import com.lawding.leavecalc.domain.AnnualLeaveContext;
import com.lawding.leavecalc.domain.DatePeriod;
import com.lawding.leavecalc.domain.flow.FlowResult;
import com.lawding.leavecalc.repository.HolidayJdbcRepository;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.List;
import java.util.Map;

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
//            DatePeriod accrualPeriod = new DatePeriod(hireDate, referenceDate);
//            return FlowResult.builder()
//                .leaveType(LeaveType.MONTHY)
//                .accrualPeriod(accrualPeriod)
//                .build();
        }
        DatePeriod accrualPeriod = getAccrualPeriodAfterFirstRegularFiscalYearStartDate(referenceDate, fiscalYear);
        // 출근율 계산하기
        return null;
    }

    private boolean isBeforeFirstFiscalYear(LocalDate referenceDate,
        LocalDate firstRegularFiscalYearStartDate) {
        return referenceDate.isBefore(firstRegularFiscalYearStartDate);
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
     *
     * @param referenceDate     산정 기준일
     * @param fiscalYear        회계연도 시작일
     * @return  정규 회계연도 시작 이후 연차 산정기간 계산 방법
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
}

