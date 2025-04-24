package com.lawding.leavecalc.strategy;

import static com.lawding.leavecalc.util.AnnualLeaveHelper.*;

import com.lawding.leavecalc.domain.AnnualLeaveContext;
import com.lawding.leavecalc.domain.AnnualLeaveResult;
import com.lawding.leavecalc.domain.DatePeriod;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;


public final class HireDateStrategy implements CalculationStrategy {

    /**
     * @param annualLeaveContext
     * @return I) 입사일 방식 - 입사일이 1년 미만일 경우 => 연차 산정 단위 기간 = [입사일, 기준일] n = 기준일과 입사일 사이의 총 기간의 연수 =
     * 근속년수? - 입사일이 1년 이상일 경우 => 연차 산정 단위 기간 = [입사일 + (근속년수 - 1), 입사일 + 근속년수 - 1일] Q. 기간 마지막 날 =
     * (기준년도 입사일 - 1일) 해도 되는건가? No 예시) 입사일 : 2020-03-09 , 기준연도 2024-02-25 => n = 3, [2022-03-09,
     * 2023-03-08] 이 맞나? 나머지 2년은 어디감? n년 산정 방식 과 마지막 1년(~기준 날짜)까지 산정 방식이 다른가? n년은 신경 안쓰나? 어차피 1년 안에
     * 무조건 다써야하나?
     * <p>
     * Q. 입사일 2023-03-21 / 기준일 2024-03-20,21,22 인 경우? 혹은 한달내(2.22 ~ 3.20)까지 같은 처리?
     */
    @Override
    public AnnualLeaveResult annualLeaveCalculate(AnnualLeaveContext annualLeaveContext) {
        /**
         * @param hireDate              입사일
         * @param referenceDate         기준일(연차 산정 기준일)
         * @param excludedWorkPeriod    근무 제외 기간
         * @param companyHolidays       회사 공휴일
         *
         *   1. 입사일이 1년 이상인지 아닌지
         */
        LocalDate hireDate = annualLeaveContext.getHireDate();
        LocalDate referenceDate = annualLeaveContext.getReferenceDate();
        Map<Integer, List<DatePeriod>> nonWorkingPeriods = annualLeaveContext.getNonWorkingPeriods();
        List<LocalDate> companyHolidays = annualLeaveContext.getCompanyHolidays();
        int annualLeaveDays = 0;
        if (isLessThanOneYear(hireDate, referenceDate)) {
            // 입사일 1년 미만
            LocalDate periodStart = hireDate;
            List<DatePeriod> excludedPeriods = nonWorkingPeriods.getOrDefault(2, List.of());
            while (true) { // 한달 단위로 개근 확인
                LocalDate periodEnd = periodStart.plusMonths(1).minusDays(1);
                LocalDate accrualDate = periodEnd.plusDays(1);
                if (accrualDate.isAfter(referenceDate)) {
                    break;
                }
                if (isFullAttendance(periodStart, periodEnd, excludedPeriods)) {
                    annualLeaveDays++;
                }
                periodStart = accrualDate;
            }
        } else {
            annualLeaveDays = calculateLeaveByServiceYears(hireDate, referenceDate);
            // 입사일 1년 이상

            double attendanceRate; // 출근율
            double prescribeWorkingRatio; // 소정근로비율

        }
        return AnnualLeaveResult.builder()
            .annualLeaveDays(annualLeaveDays)
            .build();


    }
}
