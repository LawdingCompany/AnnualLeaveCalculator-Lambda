package com.lawding.leavecalc.strategy;

import static com.lawding.leavecalc.util.AnnualLeaveHelper.*;
import static com.lawding.leavecalc.constant.AnnualLeaveConstants.*;

import com.lawding.leavecalc.constant.AnnualLeaveMessages.*;
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
            List<LocalDate> holidays = holidayRepository.findWeekdayHolidays(
                new DatePeriod(hireDate, referenceDate));
            List<DatePeriod> excludedPeriods =
                filterWorkingDayOnlyPeriods(nonWorkingPeriods.getOrDefault(2, List.of()),
                    companyHolidays, holidays);
            DatePeriod period = new DatePeriod(hireDate, referenceDate.minusDays(1));
            MonthlyLeaveDetail monthlyLeaveDetail = monthlyAccruedLeaves(period, excludedPeriods);
            result = AnnualLeaveResult.builder()
                .type(AnnualLeaveResultType.MONTHLY)
                .hireDate(hireDate)
                .referenceDate(referenceDate)
                .calculationDetail(monthlyLeaveDetail)
                .explanation(HireDate.LESS_THAN_ONE_YEAR)
                .build();
        } else {
            // 입사일 1년 이상
            DatePeriod accrualPeriod = getAccrualPeriod(hireDate, referenceDate);
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
            double prescribeWorkingRatio = calculatePrescribedWorkingRatio(prescribedWorkingDays,
                excludedWorkingDays);
            if (attendanceRate < MINIMUM_WORK_RATIO) {
                // AR < 0.8 => 월차 (직전 연차 산정 기간에 대한 개근 판단에 따라 연차 부여)
                List<DatePeriod> excludedPeriods =
                    filterWorkingDayOnlyPeriods(nonWorkingPeriods.getOrDefault(2, List.of()),
                        companyHolidays, holidays);
                MonthlyLeaveDetail monthlyLeaveDetail = monthlyAccruedLeaves(accrualPeriod,
                    excludedPeriods);
                result = AnnualLeaveResult.builder()
                    .type(AnnualLeaveResultType.MONTHLY)
                    .hireDate(hireDate)
                    .referenceDate(referenceDate)
                    .calculationDetail(monthlyLeaveDetail)
                    .explanation(HireDate.AR_UNDER_80_AFTER_ONE_YEAR)
                    .build();
            } else {
                int servicesYears = calculateServiceYears(hireDate, referenceDate);
                int addtionalLeave = calculateAdditionalLeave(servicesYears);
                if (prescribeWorkingRatio < MINIMUM_WORK_RATIO) {
                    // PWR < 0.8 => (기본연차 + 가산연차) * PWR
                    double totalLeaveDays = formatDouble(
                        (BASE_ANNUAL_LEAVE + addtionalLeave) * prescribeWorkingRatio);
                    AdjustedAnnualLeaveDetail adjustedAnnualLeaveDetail = AdjustedAnnualLeaveDetail.builder()
                        .baseAnnualLeave(BASE_ANNUAL_LEAVE)
                        .additionalLeave(addtionalLeave)
                        .prescribedWorkingDays(prescribedWorkingDays)
                        .excludedWorkingDays(excludedWorkingDays)
                        .prescribeWorkingRatio(totalLeaveDays)
                        .totalLeaveDays(totalLeaveDays)
                        .build();
                    result = AnnualLeaveResult.builder()
                        .type(AnnualLeaveResultType.ADJUSTED)
                        .hireDate(hireDate)
                        .referenceDate(referenceDate)
                        .calculationDetail(adjustedAnnualLeaveDetail)
                        .explanation(HireDate.PWR_UNDER_80_AFTER_ONE_YEAR)
                        .build();
                } else {
                    // 기본연차 + 가산연차
                    double totalLeaveDays = BASE_ANNUAL_LEAVE + addtionalLeave;
                    FullAnnualLeaveDetail fullAnnualLeaveDetail = FullAnnualLeaveDetail.builder()
                        .baseAnnualLeave(BASE_ANNUAL_LEAVE)
                        .additionalLeave(addtionalLeave)
                        .totalLeaveDays(totalLeaveDays)
                        .build();
                    result = AnnualLeaveResult.builder()
                        .type(AnnualLeaveResultType.FULL)
                        .hireDate(hireDate)
                        .referenceDate(referenceDate)
                        .calculationDetail(fullAnnualLeaveDetail)
                        .explanation(HireDate.AR_AND_PWR_OVER_80_AFTER_ONE_YEAR)
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
