package com.lawding.leavecalc.strategy;

import static com.lawding.leavecalc.constant.AnnualLeaveConstants.*;
import static com.lawding.leavecalc.util.AnnualLeaveHelper.*;
import static com.lawding.leavecalc.util.AnnualLeaveHelper.caculateDaysBetween;

import com.lawding.leavecalc.domain.AnnualLeaveContext;
import com.lawding.leavecalc.domain.AnnualLeaveResult;
import com.lawding.leavecalc.domain.DatePeriod;
import com.lawding.leavecalc.repository.HolidayJdbcRepository;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.Date;
import java.util.List;
import java.util.Map;

public final class FiscalYearStrategy implements CalculationStrategy {

    private final HolidayJdbcRepository holidayRepository;

    public FiscalYearStrategy(HolidayJdbcRepository holidayRepository) {
        this.holidayRepository = holidayRepository;
    }

    /**
     * @param annualLeaveContext 계산할 연차 정보를 담은 객체
     * @return 산정방식(회계연도)을 적용해 발생한 연차 개수
     * <p>
     */
    @Override
    public AnnualLeaveResult annualLeaveCalculate(AnnualLeaveContext annualLeaveContext) {
        LocalDate hireDate = annualLeaveContext.getHireDate();
        LocalDate referenceDate = annualLeaveContext.getReferenceDate();
        MonthDay fiscalYear = annualLeaveContext.getFiscalYear();
        Map<Integer, List<DatePeriod>> nonWorkingPeriods = annualLeaveContext.getNonWorkingPeriods();
        List<LocalDate> companyHolidays = annualLeaveContext.getCompanyHolidays();
        double annualLeaveDays = 0;
        int monthlyLeave = 0;
        double proRatedLeave = 0;
        String explanation = "";
        LocalDate firstEligibleFiscalStartDate = calculateFirstEligibleFiscalYearStart(hireDate,
            fiscalYear);
        if (referenceDate.isBefore(firstEligibleFiscalStartDate)) {
            // 기준일이 정규 연차가 처음으로 적용되는 회계연도 시작일보다 전이라면,
            LocalDate nextFiscalYearStartDate = getNextFiscalStart(hireDate, fiscalYear);
            LocalDate nextFiscalYearEndDate = nextFiscalYearStartDate.plusYears(1).minusDays(1);
            List<DatePeriod> excludedPeriods = nonWorkingPeriods.getOrDefault(2, List.of());
            if (referenceDate.isBefore(nextFiscalYearStartDate)) {
                // 기준일이 입사일과 같은 회계연도이면(< 다음 회계연도 시작일)
                // [입사일 - 기준일 - 회계연도 종료일][회계연도 시작일 - 입사후 1년][입사후 1년 - 회계연도 종료일]
                annualLeaveDays = monthlyAccruedLeaves(hireDate, referenceDate, excludedPeriods);
                explanation = "산정 방식(회계연도)에 따라 계산한 결과, 산정일 기준 1년 미만이므로 매월 개근 판단하여 연차가 부여됌";
            } else {
                // 기준일이 다음 회계연도 기간 중에 있다면
                /**
                 * [입사일 - 회계연도 종료일][회계연도 시작일 - 기준일 - 회계연도 종료일][첫 정규 회계연도]
                 * prev - next - firstEligible
                 */
                monthlyLeave = monthlyAccruedLeaves(hireDate, referenceDate, excludedPeriods);
                LocalDate prevFiscalYearStartDate = nextFiscalYearStartDate.minusYears(1);
                LocalDate prevFiscalYearEndDate = nextFiscalYearStartDate.minusDays(1);
                DatePeriod prevPeriod = new DatePeriod(prevFiscalYearStartDate,
                    prevFiscalYearEndDate);
                DatePeriod myPeriod = new DatePeriod(hireDate, prevFiscalYearEndDate);
                double proRate = calculateProRate(myPeriod, prevPeriod);
                proRatedLeave = BASE_ANNUAL_LEAVE * proRate;
                explanation =
                    hireDate + " ~ " + minDate(referenceDate, hireDate.plusYears(1))
                    + "까지 쓸 수 있는 월차는"
                    + monthlyLeave + "개 입니다. 또한, " + nextFiscalYearStartDate + " ~ "
                    + nextFiscalYearEndDate + "까지 쓸 수 있는 비례 연차는" + proRatedLeave + "개 입니다.";
            }
        } else {
            // 기준일이 정규 연차가 처음으로 적용되는 시작일보다 뒤라면,
            // 기준일에 해당하는 회계연도를 구해
            int serviceYears = calculateServiceYears(referenceDate, firstEligibleFiscalStartDate);
            int additionalLeave = calculateAdditionalLeave(serviceYears);
            DatePeriod period = getPreviousAccrualPeriod(referenceDate, fiscalYear);
            List<LocalDate> holidays = holidayRepository.findWeekdayHolidays(period);
            int prescribedWokringDays = calculatePrescribedWorkingDays(period, companyHolidays,
                holidays);
            double attendanceRate = calculateAttendanceRate(prescribedWokringDays,
                nonWorkingPeriods, companyHolidays, holidays);
            if (attendanceRate >= MINIMUM_WORK_RATIO) {
                double prescribeWorkingRatio = calculatePrescribedWorkingRatio(
                    prescribedWokringDays, nonWorkingPeriods, companyHolidays, holidays);
                annualLeaveDays = (BASE_ANNUAL_LEAVE + additionalLeave) * prescribeWorkingRatio;
                explanation =
                    referenceDate + " 기준 사용하실 수 있는 연차를 산정하는 기간은" + period.startDate() + " ~ "
                    + period.endDate()
                    + "입니다. 해당 기간 내 근무 형태 분석 결과, 출근율(AR) 80% 이상이므로 근속연수에 따른 연차에 PWR(소정근로비율)을 적용해 총 연차는 "
                    + annualLeaveDays + "개 입니다.";
            } else {
                List<DatePeriod> excludedPeriods = nonWorkingPeriods.getOrDefault(2, List.of());
                annualLeaveDays = monthlyAccruedLeaves(period.startDate(), period.endDate(),
                    excludedPeriods);
                explanation =
                    referenceDate + " 기준 사용하실 수 있는 연차를 산정하는 기간은" + period.startDate() + " ~ "
                    + period.endDate()
                    + "입니다. 해당 기간 내 근무 형태 분석 결과, 출근율(AR) 80% 미만이므로 매월 개근 여부에 따라 월차가 부여하는 방식으로 분석한 결과 "
                    + annualLeaveDays + "개 입니다.";
            }
        }

        return AnnualLeaveResult.builder()
            .annualLeaveDays(annualLeaveDays)
            .explanation(explanation)
            .build();
    }


    /**
     * 입사 1주년 이후 처음으로 정규 연차가 발생하는 회계연도 시작일을 계산하는 함수
     * <p>
     * 예시) 회계연도 = 01-01 <p> 입사일 = 2024-01-01인 경우, 다음 회계연도 시작일 = 2025-01-01 <p>입사일 = 2024-03-02인 경우,
     * 다음 회계연도 시작일 = 2026-01-01
     *
     * @param hireDate   입사일
     * @param fiscalYear 회계연도 시작일("MM-dd")
     * @return 정규 연차가 처음 적용되는 회계연도 시작일
     */
    private static LocalDate calculateFirstEligibleFiscalYearStart(LocalDate hireDate,
        MonthDay fiscalYear) {
        LocalDate oneYearAnniversaryDate = hireDate.plusYears(1); // 2025-01-01
        LocalDate nextFiscalYear = fiscalYear.atYear(
            oneYearAnniversaryDate.getYear()); // 2025-02-01
        if (oneYearAnniversaryDate.isAfter(nextFiscalYear)) {
            // 입사 1주년 > 회계연도 시작일 => 다음해 회계연도 시작일
            nextFiscalYear = nextFiscalYear.plusYears(1);
        }
        return nextFiscalYear;
    }

    /***
     *
     * @param referenceDate                     기준일
     * @param firstEligibleFiscalStartDate      첫 정규 연차 발생 회계연도
     * @return 근속연수
     */
    private static int calculateServiceYears(LocalDate referenceDate,
        LocalDate firstEligibleFiscalStartDate) {
        if (referenceDate.isBefore(firstEligibleFiscalStartDate)) {
            return 0;
        }
        return referenceDate.getYear() - firstEligibleFiscalStartDate.getYear() + 1;
    }


    /**
     * @param hireDate   입사일
     * @param fiscalYear 회계연도
     * @return 입사 후 다음 회계연도 시작일
     */
    private static LocalDate getNextFiscalStart(LocalDate hireDate, MonthDay fiscalYear) {
        LocalDate fiscalStart = fiscalYear.atYear(hireDate.getYear());
        return hireDate.isBefore(fiscalStart) ? fiscalStart : fiscalStart.plusYears(1);
    }


    /**
     * @param date1 날짜1
     * @param date2 날짜2
     * @return 날짜1, 날짜2 중 가장 최근 날짜를 리턴
     */
    private static LocalDate minDate(LocalDate date1, LocalDate date2) {
        return date1.isBefore(date2) ? date1 : date2;
    }

    /**
     * @param workPeriod  근속기간 총일수
     * @param totalPeriod 회계연도
     * @return 비례연차 비율
     */
    private static double calculateProRate(DatePeriod workPeriod, DatePeriod totalPeriod) {
        int numerator = caculateDaysBetween(workPeriod);
        int denominator = caculateDaysBetween(totalPeriod);
        return formatDouble((double) numerator / denominator);
    }

    private static DatePeriod getPreviousAccrualPeriod(LocalDate referenceDate,
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

    /**
     * @param prescribedWorkingDays 소정근로일 수(연차 산정 기간에서의 근무날 수)
     * @param nonWorkingPeriods     타입(1,2,3)에 따라 비근무 기간을 저정한 배열
     *                              <p>
     * @return PWR(소정근로비율) = (소정근로일 수 - 소정근로제외일 수) / 소정근로일 수
     */
    private static double calculatePrescribedWorkingRatio(int prescribedWorkingDays,
        Map<Integer, List<DatePeriod>> nonWorkingPeriods, List<LocalDate> companyHolidays,
        List<LocalDate> statutoryHolidays) {
        List<DatePeriod> excludedWorkPeriods = nonWorkingPeriods.getOrDefault(3, List.of());
        int excludeWorkingDays = calculateTotalDaysFromPeriods(excludedWorkPeriods, companyHolidays,
            statutoryHolidays);
        int numerator = prescribedWorkingDays - excludeWorkingDays;
        return formatDouble((double) numerator / prescribedWorkingDays);
    }


}
