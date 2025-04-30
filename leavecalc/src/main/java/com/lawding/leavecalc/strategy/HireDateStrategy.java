package com.lawding.leavecalc.strategy;

import static com.lawding.leavecalc.util.AnnualLeaveHelper.*;
import static com.lawding.leavecalc.constant.AnnualLeaveConstants.*;

import com.lawding.leavecalc.domain.AnnualLeaveContext;
import com.lawding.leavecalc.domain.AnnualLeaveResult;
import com.lawding.leavecalc.domain.DatePeriod;
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
        double annualLeaveDays = 0;
        String explanation = "";
        if (isLessThanOneYear(hireDate, referenceDate)) {
            // 입사일 1년 미만 => 월차
            List<DatePeriod> excludedPeriods = nonWorkingPeriods.getOrDefault(2, List.of());
            annualLeaveDays = monthlyAccruedLeaves(hireDate, referenceDate, excludedPeriods);
            explanation = "산정 방식(입사일)에 따라 계산한 결과, 산정일 기준 1년 미만이므로 매월 개근 판단하여 연차가 부여됌";
        } else {
            // 입사일 1년 이상
            int servicesYears = calculateServiceYears(hireDate, referenceDate);
            int addtionalLeave = calculateAdditionalLeave(servicesYears);
            DatePeriod accrualPeriod = getAccrualPeriod(hireDate,
                referenceDate);
            System.out.println(
                "연차 산정 기간 : " + accrualPeriod.startDate() + " ~ " + accrualPeriod.endDate());
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
                List<DatePeriod> excludedPeriods = nonWorkingPeriods.getOrDefault(2, List.of());
                annualLeaveDays = monthlyAccruedLeaves(accrualPeriod.startDate(),
                    accrualPeriod.endDate(), excludedPeriods);
                explanation = "산정 방식(입사일)에 따라 계산한 결과, 기준일 직전 연차 산정 기간에 대해 출근율(AR) 80% 미만이므로 "
                    + "매월 개근 판단하여 연차가 부여됌";
            } else {
                if (prescribeWorkingRatio < MINIMUM_WORK_RATIO) {
                    // PWR < 0.8 => (기본연차 + 가산연차) * PWR
                    annualLeaveDays = formatDouble(
                        (BASE_ANNUAL_LEAVE + addtionalLeave) * prescribeWorkingRatio);
                    explanation =
                        "산정 방식(입사일)에 따라 계산한 결과, 기준일 직전 연차 산정 기간에 대해 출근율(AR) 80% 이상, 소정근로비율(PWR) 80% 미만이므로 "
                            + "소정근로비율(PWR)만큼 가산 연차를 계산해 적용합니다.";
                } else {
                    // 기본연차 + 가산연차
                    annualLeaveDays = BASE_ANNUAL_LEAVE + addtionalLeave;
                    explanation =
                        "산정 방식(입사일)에 따라 계산한 결과, 기준일 직전 연차 산정 기간에 대해 출근율(AR) 80% 이상, 소정근로비율(PWR) 80% 이상이므로 "
                            + "근속연수에 따른 연차가 지급됩니다.";
                }
            }

        }
        return AnnualLeaveResult.builder()
            .annualLeaveDays(annualLeaveDays)
            .explanation(explanation)
            .build();


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
