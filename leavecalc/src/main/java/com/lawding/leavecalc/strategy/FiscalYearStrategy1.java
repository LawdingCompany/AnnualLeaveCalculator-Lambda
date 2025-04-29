package com.lawding.leavecalc.strategy;

import static com.lawding.leavecalc.constant.AnnualLeaveConstants.BASE_ANNUAL_LEAVE;
import static com.lawding.leavecalc.constant.AnnualLeaveConstants.MINIMUM_WORK_RATIO;
import static com.lawding.leavecalc.util.AnnualLeaveHelper.calculateAdditionalLeave;
import static com.lawding.leavecalc.util.AnnualLeaveHelper.calculateAttendanceRate;
import static com.lawding.leavecalc.util.AnnualLeaveHelper.calculatePrescribedWorkingDays;
import static com.lawding.leavecalc.util.AnnualLeaveHelper.calculatePrescribedWorkingRatio;
import static com.lawding.leavecalc.util.AnnualLeaveHelper.formatDouble;
import static com.lawding.leavecalc.util.AnnualLeaveHelper.isLessThanOneYear;
import static com.lawding.leavecalc.util.AnnualLeaveHelper.monthlyAccruedLeaves;

import com.lawding.leavecalc.domain.AnnualLeaveContext;
import com.lawding.leavecalc.domain.AnnualLeaveResult;
import com.lawding.leavecalc.domain.DatePeriod;
import com.lawding.leavecalc.repository.HolidayJdbcRepository;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.List;
import java.util.Map;

public final class FiscalYearStrategy1 implements CalculationStrategy {

    private final HolidayJdbcRepository holidayRepository;

    public FiscalYearStrategy1(HolidayJdbcRepository holidayRepository) {
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
        int monthlyLeaves = 0;
        String explanation = "";
        if (isLessThanOneYear(hireDate, referenceDate)) {
            // 입사 후 1년 미만인 경우 => 월차
            List<DatePeriod> excludedPeriods = nonWorkingPeriods.getOrDefault(2, List.of());
            monthlyLeaves = monthlyAccruedLeaves(hireDate, referenceDate, excludedPeriods);
            explanation = "입사 후 1년 미만이므로, 개근 여부 판단에 따라 " + monthlyLeaves + "개입니다.";
        } else {
            LocalDate firstRegularFiscalYearStartDate = calculateFirstRegularFiscalYearStartDate(
                hireDate, fiscalYear);
            if (firstRegularFiscalYearStartDate.isBefore(referenceDate)) {
                // 산정 기준일이 첫 정기 회계연도보다 이전이면,
                // 연차 산정 기간 = [입사일, 입사후1년미만]
                DatePeriod period = new DatePeriod(hireDate, hireDate.plusYears(1).minusDays(1));
                List<LocalDate> holidays = holidayRepository.findWeekdayHolidays(period);
                int prescribedWorkingDays = calculatePrescribedWorkingDays(period, companyHolidays,
                    holidays);
                double attendanceRate = calculateAttendanceRate(prescribedWorkingDays,
                    nonWorkingPeriods, companyHolidays, holidays);
                if (attendanceRate < MINIMUM_WORK_RATIO) {
                    // AR < 0.8 이면 => 월차 & 입사일 ~ 입사 후 1년 미만의 기간 : 연차 산정기간
                    List<DatePeriod> excludedPeriods = nonWorkingPeriods.getOrDefault(2, List.of());
                    monthlyLeaves = monthlyAccruedLeaves(period.startDate(), period.endDate(),
                        excludedPeriods);
                    explanation =
                        "입사 후 1년 간 출근율(AR)을 바탕으로 비례 연차가 주어집니다. 그러나 귀하의 경우, "
                        + "해당 기간 출근율(AR)이 80% 미만이므로 "
                        + referenceDate + "기준 사용 가능한 연차는 해당 기간 개근 여부를 판단해"
                        + monthlyLeaves + "개 입니다.";
                } else {
                    // 15 * 비례 연차
                    // 연간소정근로비율에 대해선 산정일 전의 회계연도를 바탕으로 소정근로일 수를 계산한다.
                    // 산정일 이전 회계연도를 구해보자.
                    DatePeriod previousAccrualPeriod = getPreviousAccrualPeriod(referenceDate,
                        fiscalYear);
                    int prescribedWorkingDaysByPreviousAccrualPeriod = calculatePrescribedWorkingDays(
                        previousAccrualPeriod, companyHolidays, holidays);
                    double prescribeWorkingRatio = calculatePrescribedWorkingRatio(
                        prescribedWorkingDaysByPreviousAccrualPeriod, nonWorkingPeriods,
                        companyHolidays, holidays);
                    annualLeaveDays = formatDouble(BASE_ANNUAL_LEAVE * prescribeWorkingRatio);
                    explanation = "입사 후 1년 간 출근율(AR)을 바탕으로 비례 연차가 주어집니다."
                                  + "해당 기간 출근율(AR)이 80% 미만이므로 "
                                  + referenceDate + "기준 사용 가능한 연차는 비례 연차 계산법에 따라"
                                  + annualLeaveDays + "개 입니다.";
                }
            } else {
                // 산정 기준일이 첫 정기 회계연도보다 같거나 이후면,
                int serviceYears = calculateServiceYears(referenceDate,
                    firstRegularFiscalYearStartDate);
                int additionalLeave = calculateAdditionalLeave(serviceYears);
                DatePeriod period = getPreviousAccrualPeriod(referenceDate, fiscalYear);
                List<LocalDate> holidays = holidayRepository.findWeekdayHolidays(period);
                int prescribedWorkingDays = calculatePrescribedWorkingDays(period, companyHolidays,
                    holidays);
                double attendanceRate = calculateAttendanceRate(prescribedWorkingDays,
                    nonWorkingPeriods, companyHolidays, holidays);
                if (attendanceRate < MINIMUM_WORK_RATIO) {
                    // AR < 0.8 => 월차
                    List<DatePeriod> excludedPeriods = nonWorkingPeriods.getOrDefault(2, List.of());
                    monthlyLeaves = monthlyAccruedLeaves(period.startDate(), period.endDate(),
                        excludedPeriods);
                    explanation =
                        "산정 기준일 직전 연차 산정 기간인 " + period.startDate() + " ~ " + period.endDate()
                        + "까지 출근율을 바탕으로 연차가 주어집니다."
                        + " 그러나 귀하의 경우, "
                        + "해당 기간 출근율(AR)이 80% 미만이므로 "
                        + referenceDate + "기준 사용 가능한 연차는 연차 산정 기간 개근 여부를 판단해"
                        + monthlyLeaves + "개 입니다.";
                } else {
                    double prescribeWorkingRatio = calculatePrescribedWorkingRatio(
                        prescribedWorkingDays, nonWorkingPeriods, companyHolidays, holidays);
                    if (prescribeWorkingRatio < MINIMUM_WORK_RATIO) {
                        // PWR < 0.8 이면
                        annualLeaveDays = formatDouble(
                            (BASE_ANNUAL_LEAVE + additionalLeave) * prescribeWorkingRatio);
                    } else {
                        annualLeaveDays = BASE_ANNUAL_LEAVE + additionalLeave;
                    }
                }
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
     * @return 정규 연차가 처음 적용되는 회계연도 시작일 첫 회계연도 정규 연차 발생일 입사일 = 회계연도 시작일 => 다음 해 회계연도 시작일 입사일 != 회계연도
     * 시작일 => 다다음해 회계연도 시작일 (> <)
     * <p>
     * 입사 1주년 구해 1주년에 해당하는 회계연도를 구해 1/1 = !=
     * <p>
     * <- 6/1 -> < = > 입사 1주년  > 회계연도시작일 =>
     */
    private static LocalDate calculateFirstRegularFiscalYearStartDate(LocalDate hireDate,
        // 2024-03-02                   // 입사일 : 2024-07-01
        MonthDay fiscalYear) {
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

    /***
     *
     * @param referenceDate                     기준일
     * @param firstRegularFiscalYearStartDate   첫 정기 회계연도 시작일
     * @return 근속연수
     */
    private static int calculateServiceYears(LocalDate referenceDate,
        LocalDate firstRegularFiscalYearStartDate) { // 첫 회계연도 정규 연차 발생일 // 첫 2025년(1년차)  / 2027년=?3년차  // 6-1  => 2025-06-01                   2026-06-01      00     20270601 00
        if (referenceDate.isBefore(firstRegularFiscalYearStartDate)) {
            return 0; // ㅇ연도로만 구분x 전 후 비교해야댐
        }
        return referenceDate.getYear() - firstRegularFiscalYearStartDate.getYear() + 1;
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


}
