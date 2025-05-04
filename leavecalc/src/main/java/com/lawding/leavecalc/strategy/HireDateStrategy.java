package com.lawding.leavecalc.strategy;

import static com.lawding.leavecalc.constant.AnnualLeaveMessages.*;
import static com.lawding.leavecalc.util.AnnualLeaveHelper.*;
import static com.lawding.leavecalc.constant.AnnualLeaveConstants.*;

import com.lawding.leavecalc.domain.AnnualLeaveContext;
import com.lawding.leavecalc.domain.detail.AdjustedAnnualLeaveDetail;
import com.lawding.leavecalc.domain.detail.FullAnnualLeaveDetail;
import com.lawding.leavecalc.domain.detail.MonthlyLeaveDetail;
import com.lawding.leavecalc.domain.AnnualLeaveResult;
import com.lawding.leavecalc.domain.DatePeriod;
import com.lawding.leavecalc.domain.AnnualLeaveResultType;
import com.lawding.leavecalc.repository.HolidayJdbcRepository;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Map;


public final class HireDateStrategy implements CalculationStrategy {

    private final HolidayJdbcRepository holidayRepository;

    public HireDateStrategy(HolidayJdbcRepository holidayRepository) {
        this.holidayRepository = holidayRepository;
    }

    /**
     * @param annualLeaveContext 계산할 연차 정보를 담고 있는 객체
     * @return 산정방식(입사일)을 적용해 발생한 연차 개수
     */
    @Override
    public AnnualLeaveResult annualLeaveCalculate(AnnualLeaveContext annualLeaveContext) {
        LocalDate hireDate = annualLeaveContext.getHireDate();
        LocalDate referenceDate = annualLeaveContext.getReferenceDate();
        Map<Integer, List<DatePeriod>> nonWorkingPeriods = annualLeaveContext.getNonWorkingPeriods();
        List<LocalDate> companyHolidays = annualLeaveContext.getCompanyHolidays();
        AnnualLeaveResult result = null;
        if (isLessThanOneYear(hireDate, referenceDate)) {
            // 입사일 1년 미만 => 월차
            List<LocalDate> holidaysWithinMonthlyLeaveAccrualPeriod = holidayRepository.findWeekdayHolidays(
                new DatePeriod(hireDate, referenceDate));
            List<DatePeriod> absentPeriods =
                filterWorkingDayOnlyPeriods(nonWorkingPeriods.getOrDefault(2, List.of()),
                    companyHolidays, holidaysWithinMonthlyLeaveAccrualPeriod);
            DatePeriod monthlyLeaveAccrualPeriod = new DatePeriod(hireDate, referenceDate.minusDays(1));
            MonthlyLeaveDetail monthlyLeaveDetail = monthlyAccruedLeaves(monthlyLeaveAccrualPeriod, absentPeriods);
            result = AnnualLeaveResult.builder()
                .type(AnnualLeaveResultType.MONTHLY)
                .hireDate(hireDate)
                .referenceDate(referenceDate)
                .calculationDetail(monthlyLeaveDetail)
                .explanation(LESS_THAN_ONE_YEAR)
                .build();
        } else {
            // 입사일 1년 이상
            DatePeriod accrualPeriod = getAccrualPeriod(hireDate, referenceDate);
            List<LocalDate> holidaysWithinAccrualPeriod = holidayRepository.findWeekdayHolidays(accrualPeriod);
            int prescribedWorkingDays = calculatePrescribedWorkingDays(accrualPeriod,
                companyHolidays, holidaysWithinAccrualPeriod);
            int absentDays = calculatePrescribedWorkingDaysWithinPeriods(
                nonWorkingPeriods.getOrDefault(2, List.of()), accrualPeriod,
                companyHolidays, holidaysWithinAccrualPeriod);
            int excludedWorkingDays = calculatePrescribedWorkingDaysWithinPeriods(
                nonWorkingPeriods.getOrDefault(3, List.of()), accrualPeriod,
                companyHolidays,
                holidaysWithinAccrualPeriod);
            double attendanceRate = calculateAttendanceRate(prescribedWorkingDays,
                absentDays, excludedWorkingDays);
            double prescribeWorkingRatio = calculatePrescribedWorkingRatio(prescribedWorkingDays,
                excludedWorkingDays);
            if (attendanceRate < MINIMUM_WORK_RATIO) {
                // AR < 0.8 => 월차 (직전 연차 산정 기간에 대한 개근 판단에 따라 연차 부여)
                List<DatePeriod> absentPeriods =
                    filterWorkingDayOnlyPeriods(nonWorkingPeriods.getOrDefault(2, List.of()),
                        companyHolidays, holidaysWithinAccrualPeriod);
                MonthlyLeaveDetail monthlyLeaveDetail = monthlyAccruedLeaves(accrualPeriod,
                    absentPeriods);
                result = AnnualLeaveResult.builder()
                    .type(AnnualLeaveResultType.MONTHLY)
                    .hireDate(hireDate)
                    .referenceDate(referenceDate)
                    .calculationDetail(monthlyLeaveDetail)
                    .explanation(AR_UNDER_80_AFTER_ONE_YEAR)
                    .build();
            } else {
                int serviceYears = calculateServiceYears(hireDate, referenceDate);
                int additionalLeave = calculateAdditionalLeave(serviceYears);
                if (prescribeWorkingRatio < MINIMUM_WORK_RATIO) {
                    // PWR < 0.8 => (기본연차 + 가산연차) * PWR
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
                    result = AnnualLeaveResult.builder()
                        .type(AnnualLeaveResultType.ADJUSTED)
                        .hireDate(hireDate)
                        .referenceDate(referenceDate)
                        .calculationDetail(adjustedAnnualLeaveDetail)
                        .explanation(AR_OVER_80_PWR_UNDER_80_AFTER_ONE_YEAR)
                        .build();
                } else {
                    // 기본연차 + 가산연차
                    double totalLeaveDays = BASE_ANNUAL_LEAVE + additionalLeave;
                    FullAnnualLeaveDetail fullAnnualLeaveDetail = FullAnnualLeaveDetail.builder()
                        .accrualPeriod(accrualPeriod)
                        .baseAnnualLeave(BASE_ANNUAL_LEAVE)
                        .serviceYears(serviceYears)
                        .additionalLeave(additionalLeave)
                        .totalLeaveDays(totalLeaveDays)
                        .build();
                    result = AnnualLeaveResult.builder()
                        .type(AnnualLeaveResultType.FULL)
                        .hireDate(hireDate)
                        .referenceDate(referenceDate)
                        .calculationDetail(fullAnnualLeaveDetail)
                        .explanation(AR_AND_PWR_OVER_80_AFTER_ONE_YEAR)
                        .build();
                }
            }

        }
        return result;

    }

    /**
     * 근속연수를 계산하는 함수
     *
     * @param startDate 시작일
     * @param endDate   종료일
     * @return 근속연수
     */
    private static int calculateServiceYears(LocalDate startDate, LocalDate endDate) {
        return Period.between(startDate, endDate).getYears();
    }


    /**
     * 산정일 기준 직전 연차 산정 기간을 계산하는 함수
     *
     * @param hireDate      입사일
     * @param referenceDate 기준일
     * @return 연차 산정 단위 기간 [시작일, 종료일]
     */
    private static DatePeriod getAccrualPeriod(LocalDate hireDate,
        LocalDate referenceDate) {
        int years = calculateServiceYears(hireDate, referenceDate) - 1;
        LocalDate start = hireDate.plusYears(years);
        LocalDate end = start.plusYears(1).minusDays(1);
        return new DatePeriod(start, end);
    }
}
