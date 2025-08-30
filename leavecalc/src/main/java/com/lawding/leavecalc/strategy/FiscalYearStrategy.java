package com.lawding.leavecalc.strategy;

import static com.lawding.leavecalc.constant.AnnualLeaveConstants.*;
import static com.lawding.leavecalc.constant.AnnualLeaveMessages.*;
import static com.lawding.leavecalc.util.AnnualLeaveHelper.*;

import com.lawding.leavecalc.domain.AnnualLeaveContext;
import com.lawding.leavecalc.dto.AnnualLeaveResult;
import com.lawding.leavecalc.domain.AnnualLeaveResultType;
import com.lawding.leavecalc.domain.CalculationType;
import com.lawding.leavecalc.domain.DatePeriod;
import com.lawding.leavecalc.dto.detail.AdjustedAnnualLeaveDetail;
import com.lawding.leavecalc.dto.detail.FullAnnualLeaveDetail;
import com.lawding.leavecalc.dto.detail.MonthlyLeaveDetail;
import com.lawding.leavecalc.dto.detail.MonthlyProratedAnnualLeaveDetail;
import com.lawding.leavecalc.dto.detail.ProratedAnnualLeaveDetail;
import com.lawding.leavecalc.flow.FiscalYearFlow;
import com.lawding.leavecalc.repository.HolidayJdbcRepository;
import com.lawding.leavecalc.util.AnnualLeaveHelper;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class FiscalYearStrategy implements CalculationStrategy {

    private final HolidayJdbcRepository holidayRepository;

    public FiscalYearStrategy(FiscalYearFlow holidayRepository) {
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
        List<DatePeriod> absentPeriods = nonWorkingPeriods.getOrDefault(2, List.of());
        List<DatePeriod> excludedPeriods = nonWorkingPeriods.getOrDefault(3, List.of());
        List<LocalDate> companyHolidays = annualLeaveContext.getCompanyHolidays();
        LocalDate firstRegularFiscalYearStartDate = calculateFirstRegularFiscalYearStartDate(
            hireDate, fiscalYear); // 첫 정기 회계연도
        if (referenceDate.isBefore(firstRegularFiscalYearStartDate)) {
            // 기준일 < 첫 정기 회계연도
            return calculateLeaveBeforeFirstRegularFiscalYear(hireDate, referenceDate, fiscalYear,
                absentPeriods, excludedPeriods, companyHolidays);
        } else {
            // 기준일 >= 첫 정기 회계연도
            return calculateLeaveOnOrAfterFirstRegularFiscalYear(hireDate, referenceDate,
                fiscalYear, firstRegularFiscalYearStartDate, absentPeriods, excludedPeriods,
                companyHolidays);
        }
    }

    private AnnualLeaveResult calculateLeaveOnOrAfterFirstRegularFiscalYear(LocalDate hireDate,
        LocalDate referenceDate, MonthDay fiscalYear, LocalDate firstRegularFiscalYearStartDate,
        List<DatePeriod> absentPeriods, List<DatePeriod> excludedPeriods,
        List<LocalDate> companyHolidays) {
        DatePeriod accrualPeriod = getAccrualPeriod(referenceDate, fiscalYear);
        List<LocalDate> holidaysWithinAccrualPeriod = holidayRepository.findWeekdayHolidays(
            accrualPeriod);
        int prescribedWorkingDays = calculatePrescribedWorkingDays(accrualPeriod,
            companyHolidays, holidaysWithinAccrualPeriod);
        int absentDays = calculatePrescribedWorkingDaysInAbsentPeriods(absentPeriods,
            accrualPeriod, holidaysWithinAccrualPeriod, companyHolidays, excludedPeriods);
        int excludedWorkingDays = calculateExcludedWorkingDays(excludedPeriods, accrualPeriod,
            holidaysWithinAccrualPeriod, companyHolidays);
        double attendanceRate = calculateAttendanceRate(prescribedWorkingDays, absentDays,
            excludedWorkingDays);
        if (attendanceRate < MINIMUM_WORK_RATIO) {
            Set<LocalDate> workingDaysWithinAbsentPeriods = getPrescribedWorkingDaySetInAbsentPeriods(
                absentPeriods, accrualPeriod, holidaysWithinAccrualPeriod, companyHolidays,
                excludedPeriods);
            MonthlyLeaveDetail monthlyLeaveDetail = monthlyAccruedLeaves(accrualPeriod,
                workingDaysWithinAbsentPeriods);
            return AnnualLeaveResult.builder()
                .calculationType(CalculationType.FISCAL_YEAR)
                .annualLeaveResultType(AnnualLeaveResultType.MONTHLY)
                .fiscalYear(fiscalYear)
                .hireDate(hireDate)
                .referenceDate(referenceDate)
                .calculationDetail(monthlyLeaveDetail)
                .explanation(AR_UNDER_80_AFTER_ONE_YEAR)
                .build();
        } else {
            double prescribeWorkingRatio = AnnualLeaveHelper.calculatePrescribedWorkingRatio(
                prescribedWorkingDays, excludedWorkingDays);
            int serviceYears = calculateServiceYears(referenceDate,
                firstRegularFiscalYearStartDate);
            int additionalLeave = calculateAdditionalLeave(serviceYears);
            if (prescribeWorkingRatio < MINIMUM_WORK_RATIO) {
                double totalLeaveDays = formatDouble(
                    (BASE_ANNUAL_LEAVE + additionalLeave) * prescribeWorkingRatio);
                AdjustedAnnualLeaveDetail adjustedAnnualLeaveDetail = AdjustedAnnualLeaveDetail.builder()
                    .baseAnnualLeave(BASE_ANNUAL_LEAVE)
                    .serviceYears(serviceYears)
                    .additionalLeave(additionalLeave)
                    .prescribedWorkingDays(prescribedWorkingDays)
                    .excludedWorkingDays(excludedWorkingDays)
                    .prescribeWorkingRatio(prescribeWorkingRatio)
                    .totalLeaveDays(totalLeaveDays)
                    .build();
                return AnnualLeaveResult.builder()
                    .calculationType(CalculationType.FISCAL_YEAR)
                    .annualLeaveResultType(AnnualLeaveResultType.ADJUSTED)
                    .fiscalYear(fiscalYear)
                    .hireDate(hireDate)
                    .referenceDate(referenceDate)
                    .calculationDetail(adjustedAnnualLeaveDetail)
                    .explanation(AR_OVER_80_PWR_UNDER_80_AFTER_ONE_YEAR)
                    .build();
            } else {
                double totalLeaveDays = BASE_ANNUAL_LEAVE + additionalLeave;
                FullAnnualLeaveDetail fullAnnualLeaveDetail = FullAnnualLeaveDetail.builder()
                    .accrualPeriod(accrualPeriod)
                    .baseAnnualLeave(BASE_ANNUAL_LEAVE)
                    .serviceYears(serviceYears)
                    .additionalLeave(additionalLeave)
                    .totalLeaveDays(totalLeaveDays)
                    .build();
                return AnnualLeaveResult.builder()
                    .calculationType(CalculationType.FISCAL_YEAR)
                    .annualLeaveResultType(AnnualLeaveResultType.FULL)
                    .fiscalYear(fiscalYear)
                    .hireDate(hireDate)
                    .referenceDate(referenceDate)
                    .calculationDetail(fullAnnualLeaveDetail)
                    .explanation(AR_AND_PWR_OVER_80_AFTER_ONE_YEAR)
                    .build();
            }
        }
    }

    private AnnualLeaveResult calculateLeaveBeforeFirstRegularFiscalYear(LocalDate hireDate,
        LocalDate referenceDate, MonthDay fiscalYear,
        List<DatePeriod> absentPeriods, List<DatePeriod> excludedPeriods,
        List<LocalDate> companyHolidays) {
        DatePeriod nextFiscalYear = getNextFiscalYears(hireDate, fiscalYear);
        DatePeriod monthlyLeaveAccrualPeriod =
            isBeforeOneYearFromHireDate(hireDate, referenceDate)
                ? new DatePeriod(hireDate, referenceDate.minusDays(1))
                : new DatePeriod(hireDate, hireDate.plusYears(1).minusDays(1));
        MonthlyLeaveDetail monthlyLeaveDetail = calculateMonthlyLeave(monthlyLeaveAccrualPeriod,
            absentPeriods,
            excludedPeriods, companyHolidays);
        if (referenceDate.isBefore(nextFiscalYear.startDate())) {
            // 기준일이 입사일과 같은 회계연도이면 => 월차
            return AnnualLeaveResult.builder()
                .calculationType(CalculationType.FISCAL_YEAR)
                .annualLeaveResultType(AnnualLeaveResultType.MONTHLY)
                .fiscalYear(fiscalYear)
                .hireDate(hireDate)
                .referenceDate(referenceDate)
                .calculationDetail(monthlyLeaveDetail)
                .explanation(LESS_THAN_ONE_YEAR)
                .build();
        } else {
            // 기준일이 입사일 다음 회계연도 기간 중에 있다면 => 월차 + 비례연차
            // 입사 후 1년 미만, 입사 후 1년 이상
            LocalDate prevFiscalYearEndDate = nextFiscalYear.startDate().minusDays(1);
            // 연차 산정 기간 [입사일, 회계연도 종료일]
            DatePeriod accrualPeriod = new DatePeriod(hireDate, prevFiscalYearEndDate);
            List<LocalDate> holidaysWithinAccrualPeriod = holidayRepository.findWeekdayHolidays(
                accrualPeriod);
            int prescribedWorkingDays = calculatePrescribedWorkingDays(accrualPeriod,
                companyHolidays, holidaysWithinAccrualPeriod);
            int absentDays = calculatePrescribedWorkingDaysInAbsentPeriods(absentPeriods,
                accrualPeriod, holidaysWithinAccrualPeriod, companyHolidays, excludedPeriods);
            int excludedWorkingDays = calculateExcludedWorkingDays(excludedPeriods,
                accrualPeriod,
                holidaysWithinAccrualPeriod, companyHolidays);
            double attendanceRate = calculateAttendanceRate(prescribedWorkingDays,
                absentDays, excludedWorkingDays);
            if (attendanceRate >= MINIMUM_WORK_RATIO) {
                // 여기의 PWR은 다른 PWR과 다름.
                // 기존의 PWR = 같은 산정 기간의 소정근로비율
                // 이 경우의 PWR = [입사일, 회계연도 종료일] - 이에 해당하는 소정근로제외일 수 / [회계연도 시작일 - 회계연도 종료일]의 소정근로일
                LocalDate prevFiscalYearStartDate = prevFiscalYearEndDate.minusYears(1)
                    .plusDays(1);
                DatePeriod prevFiscalYear = new DatePeriod(prevFiscalYearStartDate,
                    prevFiscalYearEndDate);
                List<LocalDate> holidaysWithinPrevPeriod = holidayRepository.findWeekdayHolidays(
                    prevFiscalYear);
                int prescribedWorkingDaysByPrevFiscalYear = calculatePrescribedWorkingDays(
                    prevFiscalYear, companyHolidays, holidaysWithinPrevPeriod);
                double prescribeWorkingRatio = calculatePrescribedWorkingRatio(
                    prescribedWorkingDays, excludedWorkingDays,
                    prescribedWorkingDaysByPrevFiscalYear);
                double proratedLeave = formatDouble(BASE_ANNUAL_LEAVE * prescribeWorkingRatio);

                if (isBeforeOneYearFromHireDate(hireDate, referenceDate)) {
                    MonthlyProratedAnnualLeaveDetail monthlyProratedAnnualLeaveDetail =
                        MonthlyProratedAnnualLeaveDetail.builder()
                            .monthlyLeaveAccrualPeriod(monthlyLeaveAccrualPeriod)
                            .monthlyLeaveDays(monthlyLeaveDetail.getTotalLeaveDays())
                            .proratedLeaveAccrualPeriod(accrualPeriod)
                            .proratedLeaveDays(proratedLeave)
                            .totalLeaveDays(
                                monthlyLeaveDetail.getTotalLeaveDays() + proratedLeave)
                            .build();
                    return AnnualLeaveResult.builder()
                        .calculationType(CalculationType.FISCAL_YEAR)
                        .annualLeaveResultType(AnnualLeaveResultType.MONTHY_PRORATED)
                        .fiscalYear(fiscalYear)
                        .hireDate(hireDate)
                        .referenceDate(referenceDate)
                        .calculationDetail(monthlyProratedAnnualLeaveDetail)
                        .explanation(FiscalYear.AR_OVER_80_LESS_THAN_ONE_YEAR)
                        .build();
                } else {
                    ProratedAnnualLeaveDetail proratedAnnualLeaveDetail =
                        ProratedAnnualLeaveDetail.builder()
                            .proratedLeaveAccrualPeriod(accrualPeriod)
                            .totalLeaveDays(proratedLeave)
                            .build();
                    return AnnualLeaveResult.builder()
                        .calculationType(CalculationType.FISCAL_YEAR)
                        .annualLeaveResultType(AnnualLeaveResultType.PRORATED)
                        .fiscalYear(fiscalYear)
                        .hireDate(hireDate)
                        .referenceDate(referenceDate)
                        .calculationDetail(proratedAnnualLeaveDetail)
                        .explanation(FiscalYear.AR_OVER_80_AFTER_THAN_ONE_YEAR)
                        .build();
                }
            } else {
                return AnnualLeaveResult.builder()
                    .calculationType(CalculationType.FISCAL_YEAR)
                    .annualLeaveResultType(AnnualLeaveResultType.MONTHLY)
                    .fiscalYear(fiscalYear)
                    .hireDate(hireDate)
                    .referenceDate(referenceDate)
                    .calculationDetail(monthlyLeaveDetail)
                    .explanation(LESS_THAN_ONE_YEAR)
                    .build();
            }
        }


    }

    private MonthlyLeaveDetail calculateMonthlyLeave(DatePeriod accrualPeriod,
        List<DatePeriod> absentPeriods, List<DatePeriod> excludedPeriods,
        List<LocalDate> companyHolidays) {
        List<LocalDate> holidays = holidayRepository.findWeekdayHolidays(accrualPeriod);
        Set<LocalDate> workingDaysWithinAbsentPeriods = getPrescribedWorkingDaySetInAbsentPeriods(
            absentPeriods, accrualPeriod, holidays, companyHolidays, excludedPeriods);
        return monthlyAccruedLeaves(accrualPeriod, workingDaysWithinAbsentPeriods);
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
    private static DatePeriod getNextFiscalYears(LocalDate hireDate, MonthDay fiscalYear) {
        LocalDate fiscalStart = fiscalYear.atYear(hireDate.getYear());

        fiscalStart = hireDate.isBefore(fiscalStart) ? fiscalStart : fiscalStart.plusYears(1);
        LocalDate fiscalEnd = fiscalStart.plusYears(1).minusDays(1);
        return new DatePeriod(fiscalStart, fiscalEnd);
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
