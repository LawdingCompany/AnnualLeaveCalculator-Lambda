package com.lawding.leavecalc.strategy;

import static com.lawding.leavecalc.constant.AnnualLeaveConstants.*;
import static com.lawding.leavecalc.util.AnnualLeaveHelper.*;

import com.lawding.leavecalc.domain.AnnualLeaveContext;
import com.lawding.leavecalc.domain.AnnualLeaveResult;
import com.lawding.leavecalc.domain.DatePeriod;
import com.lawding.leavecalc.repository.HolidayJdbcRepository;
import com.lawding.leavecalc.util.AnnualLeaveHelper;
import java.time.LocalDate;
import java.time.MonthDay;
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
        String explanation = "";
        LocalDate firstRegularFiscalYearStartDate = calculateFirstRegularFiscalYearStartDate(
            hireDate, fiscalYear); // 첫 정기 회계연도
        if (referenceDate.isBefore(firstRegularFiscalYearStartDate)) {
            // 기준일 < 첫 정기 회계연도
            LocalDate nextFiscalYearStartDate = getNextFiscalStart(hireDate, fiscalYear);
            List<DatePeriod> excludedPeriods = nonWorkingPeriods.getOrDefault(2, List.of());
            if (referenceDate.isBefore(nextFiscalYearStartDate)) {
                // 기준일이 입사일과 같은 회계연도이면 => 월차
                DatePeriod period = new DatePeriod(hireDate, referenceDate.minusDays(1));
                annualLeaveDays = monthlyAccruedLeaves(period, excludedPeriods);
                explanation = "산정 방식(회계연도)에 따라 계산한 결과, 산정일 기준 1년 미만이므로 매월 개근 판단하여 연차가 부여됌";
            } else {
                // 기준일이 입사일 다음 회계연도 기간 중에 있다면 => 월차 + 비례연차
                // 입사 후 1년 미만, 입사 후 1년 이상
                if (isLessThanOneYear(hireDate, referenceDate)) {
                    DatePeriod period = new DatePeriod(hireDate, referenceDate.minusDays(1));
                    monthlyLeave = monthlyAccruedLeaves(period, excludedPeriods);
                } else {
                    DatePeriod period = new DatePeriod(hireDate,
                        hireDate.plusYears(1).minusDays(1));
                    monthlyLeave = monthlyAccruedLeaves(period, excludedPeriods);
                }
                LocalDate prevFiscalYearEndDate = nextFiscalYearStartDate.minusDays(1);
                // 연차 산정 기간 [입사일, 회계연도 종료일]
                DatePeriod accrualPeriod = new DatePeriod(hireDate, prevFiscalYearEndDate);
                List<LocalDate> holidays = holidayRepository.findWeekdayHolidays(accrualPeriod);
                int prescribedWorkingDays = calculatePrescribedWorkingDays(accrualPeriod,
                    companyHolidays, holidays);
                int absentDays = calculatePrescribedWorkingDaysWithinPeriods(
                    nonWorkingPeriods.getOrDefault(2, List.of()), accrualPeriod,
                    companyHolidays, holidays);
                int excludedWorkingDays = calculatePrescribedWorkingDaysWithinPeriods(
                    nonWorkingPeriods.getOrDefault(3, List.of()), accrualPeriod,
                    companyHolidays,
                    holidays);
                double attendanceRate = calculateAttendanceRate(prescribedWorkingDays,
                    absentDays, excludedWorkingDays);
                if (attendanceRate >= MINIMUM_WORK_RATIO) {
                    // 여기의 PWR은 다른 PWR과 다름.
                    // 기존의 PWR = 같은 산정 기간의 소정근로비율
                    // 이 경우의 PWR = [입사일, 회계연도 종료일] - 이에 해당하는 소정근로제외일 수 / [회계연도 시작일 - 회계연도 종료일]의 소정근로일
                    LocalDate prevFiscalYearStartDate = nextFiscalYearStartDate.minusYears(1);
                    DatePeriod prevFiscalYear = new DatePeriod(prevFiscalYearStartDate,
                        prevFiscalYearEndDate);
                    List<LocalDate> holidaysByPrevFiscalYear = holidayRepository.findWeekdayHolidays(
                        prevFiscalYear);
                    int prescribedWorkingDaysByPrevFiscalYear = calculatePrescribedWorkingDays(
                        prevFiscalYear, companyHolidays, holidaysByPrevFiscalYear);

                    double prescribeWorkingRatio = calculatePrescribedWorkingRatio(
                        prescribedWorkingDays, excludedWorkingDays,
                        prescribedWorkingDaysByPrevFiscalYear);
                    double proRatedLeave = formatDouble(BASE_ANNUAL_LEAVE * prescribeWorkingRatio);

                    if (isLessThanOneYear(hireDate, referenceDate)) {
                        annualLeaveDays = monthlyLeave + proRatedLeave;
                        System.out.println("소정 근로일 수 : " + prescribedWorkingDaysByPrevFiscalYear);
                        System.out.println("입사일 - 회계연도 종료일 : " + prescribedWorkingDays);
                        System.out.println(monthlyLeave + ", " + proRatedLeave);
                    } else {
                        annualLeaveDays = proRatedLeave;
                    }
                } else {
                    annualLeaveDays = monthlyLeave;
                    explanation = "산정 방식(회계연도)에 따라 계산한 결과, 출근율(AR)이 80% 미만이므로 매월 개근 판단하여 연차가 부여됌";
                }
            }
        } else {
            // 기준일 >= 첫 정기 회계연도
            DatePeriod accrualPeriod = getAccrualPeriod(referenceDate, fiscalYear);
            List<LocalDate> holidays = holidayRepository.findWeekdayHolidays(accrualPeriod);
            int prescribedWorkingDays = calculatePrescribedWorkingDays(accrualPeriod,
                companyHolidays,
                holidays);
            int absentDays = calculatePrescribedWorkingDaysWithinPeriods(
                nonWorkingPeriods.getOrDefault(2, List.of()), accrualPeriod,
                companyHolidays, holidays);
            int excludedWorkingDays = calculatePrescribedWorkingDaysWithinPeriods(
                nonWorkingPeriods.getOrDefault(3, List.of()), accrualPeriod,
                companyHolidays,
                holidays);
            double attendanceRate = calculateAttendanceRate(prescribedWorkingDays, absentDays,
                excludedWorkingDays);
            if (attendanceRate < MINIMUM_WORK_RATIO) {
                List<DatePeriod> excludedPeriods = nonWorkingPeriods.getOrDefault(2, List.of());
                annualLeaveDays = monthlyAccruedLeaves(accrualPeriod, excludedPeriods);
                explanation = "월차";
            } else {
                double prescribeWorkingRatio = AnnualLeaveHelper.calculatePrescribedWorkingRatio(
                    prescribedWorkingDays, excludedWorkingDays);
                int serviceYears = calculateServiceYears(referenceDate,
                    firstRegularFiscalYearStartDate);
                int additionalLeave = calculateAdditionalLeave(serviceYears);
                if (prescribeWorkingRatio < MINIMUM_WORK_RATIO) {
                    annualLeaveDays = formatDouble(
                        (BASE_ANNUAL_LEAVE + additionalLeave) * prescribeWorkingRatio);
                    explanation = "(기본연차(15) + 가산연차) * PWR";
                } else {
                    annualLeaveDays = BASE_ANNUAL_LEAVE + additionalLeave;
                    explanation = "기본연차(15) + 가산연차";
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

    /***
     *
     * @param referenceDate                     기준일
     * @param firstEligibleFiscalStartDate      첫 정규 연차 발생 회계연도
     * @return 근속연수
     */
    private static int calculateServiceYears(LocalDate referenceDate,
        LocalDate firstEligibleFiscalStartDate) { // 첫 회계연도 정규 연차 발생일 // 첫 2025년(1년차)  / 2027년=?3년차  // 6-1  => 2025-06-01                   2026-06-01      00     20270601 00
        if (referenceDate.isBefore(firstEligibleFiscalStartDate)) {
            return 0; // ㅇ연도로만 구분x 전 후 비교해야댐
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


    private static DatePeriod getAccrualPeriod(LocalDate referenceDate,
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

    private static double calculatePrescribedWorkingRatio(int prescribedWorkingDays,
        int excludedWorkingDays, int prescribedWorkingDaysByPrevFiscalYear) {
        int numerator = prescribedWorkingDays - excludedWorkingDays;
        return (double) numerator / prescribedWorkingDaysByPrevFiscalYear;
    }
}
