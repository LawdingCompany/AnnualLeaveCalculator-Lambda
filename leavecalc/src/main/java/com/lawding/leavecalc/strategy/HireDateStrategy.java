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
     *
     * @param annualLeaveContext  계산할 연차 정보를 담고 있는 객체
     * @return 산정방식(입사일)을 적용해 발생한 연차 개수
     *
     * <p>
     *  I) 입사일이 1년 미만인 경우 <p>
     *  1. 입사일 ~ 산정일까지 1개월 단위로 개근 여부를 판단한다. <p>
     *  2. 개근 개월수에 따라 연차를 지급한다. <p>
     *
     *  II) 입사일이 1년 이상인 경우 <p>
     *  1. 직전 연차 산정 기간을 계산한다. <p>
     *  2. 해당 기간의 출근율(AR), 소정근로비율(PWR)을 계산한다. <p>
     *  3. AR, PWR에 따라 분기 <p>
     *  3-1) AR < 0.8 인 경우, 그 해는 1년 미만 산정 방식과 동일하게 계산한다. <p>
     *  3-2) AR >= 0.8 && PWR < 0.8 인경우, 기본 연차 + 가산연차 * PWR 만큼 연차를 지급한다. <p>
     *  3-3) AR >= 0.8 && PWR >= 0.8 인경우, 근속연수에 따라 연차를 100% 지급한다. <p>
     *
     */
    @Override
    public AnnualLeaveResult annualLeaveCalculate(AnnualLeaveContext annualLeaveContext) {
        LocalDate hireDate = annualLeaveContext.getHireDate();
        LocalDate referenceDate = annualLeaveContext.getReferenceDate();
        Map<Integer, List<DatePeriod>> nonWorkingPeriods = annualLeaveContext.getNonWorkingPeriods();
        List<LocalDate> companyHolidays = annualLeaveContext.getCompanyHolidays();
        int annualLeaveDays = 0;
        String explanation = "";
        if (isLessThanOneYear(hireDate, referenceDate)) {
            // 입사일 1년 미만
            List<DatePeriod> excludedPeriods = nonWorkingPeriods.getOrDefault(2, List.of());
            annualLeaveDays = monthlyAccruedLeaves(hireDate, referenceDate, excludedPeriods);
            explanation = "산정 방식(입사일)에 따라 계산한 결과, 산정일 기준 1년 미만이므로 매월 개근 판단하여 연차가 부여됌";
        } else { // 입사일 1년 이상
            // 입사일 ~ 산정 기준일 => [2022-03-02], [2025-06-02] => [2024-03-02 - 2025-03-01]
            int servicesYears = calculateServiceYears(hireDate, referenceDate);
            int addtionalLeave = calculateAdditionalLeave(servicesYears);
            DatePeriod previousAccrualPeriod = getPreviousAccrualPeriod(hireDate, referenceDate); // [2024-03-02 - 2025-03-01]
            List<LocalDate> holidays = holidayRepository.findWeekdayHolidays(previousAccrualPeriod);
            int prescribedWorkingDays = calculatePrescribedWorkingDays(previousAccrualPeriod, // [2024-03-02 - 2025-03-01]
                companyHolidays, holidays);
            double attendanceRate = calculateAttendanceRate(prescribedWorkingDays,
                nonWorkingPeriods, companyHolidays, holidays);
            double prescribeWorkingRatio = calculatePrescribedWorkingRatio(prescribedWorkingDays,
                nonWorkingPeriods, companyHolidays, holidays);
            if (attendanceRate < MINIMUM_WORK_RATIO) {  // AR < 0.8
                List<DatePeriod> excludedPeriods = nonWorkingPeriods.getOrDefault(2, List.of());
                annualLeaveDays = monthlyAccruedLeaves(previousAccrualPeriod.startDate(),
                    previousAccrualPeriod.endDate(), excludedPeriods);
                explanation = "산정 방식(입사일)에 따라 계산한 결과, 기준일 직전 연차 산정 기간에 대해 출근율(AR) 80% 미만이므로 "
                              + "매월 개근 판단하여 연차가 부여됌";
            } else {
                if (prescribeWorkingRatio < MINIMUM_WORK_RATIO) { // PWR1 < 0.8
                    // ***(기본 연차 + 가산연차)*PWR1
                    addtionalLeave = (int) Math.floor(addtionalLeave * prescribeWorkingRatio);
                    explanation =
                        "산정 방식(입사일)에 따라 계산한 결과, 기준일 직전 연차 산정 기간에 대해 출근율(AR) 80% 이상, 소정근로비율(PWR) 80% 미만이므로 "
                        + "소정근로비율(PWR)만큼 가산 연차를 계산해 적용합니다.";
                } else {
                    explanation =
                        "산정 방식(입사일)에 따라 계산한 결과, 기준일 직전 연차 산정 기간에 대해 출근율(AR) 80% 이상, 소정근로비율(PWR) 80% 이상이므로 "
                        + "근속연수에 따른 연차가 지급됩니다.";
                }
                annualLeaveDays = BASE_ANNUAL_LEAVE + addtionalLeave;
            }

        }
        return AnnualLeaveResult.builder()
            .annualLeaveDays(annualLeaveDays)
            .explanation(explanation)
            .build();


    }

    /**
     * 근속연수가 1년 미만인지 확인하는 함수
     *
     * @param hireDate      입사일
     * @param referenceDate 기준일
     * @return 근속연수가 1년 미만인가
     */
    private static boolean isLessThanOneYear(LocalDate hireDate, LocalDate referenceDate) {
        return referenceDate.isBefore(hireDate.plusYears(1));
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
     * @param hireDate          입사일
     * @param referenceDate     기준일
     * @return 연차 산정 단위 기간 [시작일, 종료일]
     */
    private static DatePeriod getPreviousAccrualPeriod(LocalDate hireDate, LocalDate referenceDate) {
        int years = calculateServiceYears(hireDate, referenceDate);
        LocalDate start = hireDate.plusYears(years - 1);
        LocalDate end = start.plusYears(1).minusDays(1);
        return new DatePeriod(start, end);
    }

    /**
     * @param prescribedWorkingDays 소정근로일 수(연차 산정 기간에서의 근무날 수)
     * @param nonWorkingPeriods     타입(1,2,3)에 따라 비근무 기간을 저정한 배열
     *                              <p>
     *                              (소정근로제외일 수 / 소정근로일 수) <= 0.2 이면 PWR = 1.0
     * @return PWR1(소정근로비율) = (소정근로일 수 - 소정근로제외일 수) / 소정근로일 수
     */
    private static double calculatePrescribedWorkingRatio(int prescribedWorkingDays,
        Map<Integer, List<DatePeriod>> nonWorkingPeriods, List<LocalDate> companyHolidays,
        List<LocalDate> statutoryHolidays) {
        List<DatePeriod> excludedWorkPeriods = nonWorkingPeriods.getOrDefault(3, List.of());
        int excludeWorkingDays = calculateTotalDaysFromPeriods(excludedWorkPeriods, companyHolidays,
            statutoryHolidays);
        if ((double) excludeWorkingDays / prescribedWorkingDays <= 0.2) {
            return DEFAULT_WORK_RATIO;
        }
        int numerator = prescribedWorkingDays - excludeWorkingDays;
        return formatDouble((double) numerator / prescribedWorkingDays);
        // 그냥 그 자체로 처리 올림,내림,반올림x
    }
}
