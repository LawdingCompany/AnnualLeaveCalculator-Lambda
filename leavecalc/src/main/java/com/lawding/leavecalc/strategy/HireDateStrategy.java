package com.lawding.leavecalc.strategy;

import static com.lawding.leavecalc.constant.AnnualLeaveMessages.*;
import static com.lawding.leavecalc.util.AnnualLeaveHelper.*;
import static com.lawding.leavecalc.constant.AnnualLeaveConstants.*;

import com.lawding.leavecalc.domain.AnnualLeaveContext;
import com.lawding.leavecalc.domain.CalculationType;
import com.lawding.leavecalc.domain.flow.hiredate.FullFlowResult;
import com.lawding.leavecalc.dto.AdjustedAnnualLeaveResult;
import com.lawding.leavecalc.dto.FullAnnualLeaveResult;
import com.lawding.leavecalc.dto.detail.AdjustedAnnualLeaveDetail;
import com.lawding.leavecalc.dto.detail.FullAnnualLeaveDetail;
import com.lawding.leavecalc.dto.detail.MonthlyLeaveDetail;
import com.lawding.leavecalc.dto.AnnualLeaveResult;
import com.lawding.leavecalc.domain.DatePeriod;
import com.lawding.leavecalc.domain.flow.FlowResult;
import com.lawding.leavecalc.domain.flow.hiredate.LowPWRFlowResult;
import com.lawding.leavecalc.domain.flow.hiredate.LessOneYearFlowResult;
import com.lawding.leavecalc.domain.flow.hiredate.LowARFlowResult;
import com.lawding.leavecalc.flow.CalculationFlow;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;


public final class HireDateStrategy implements CalculationStrategy {

    private final CalculationFlow flow;

    public HireDateStrategy(CalculationFlow flow) {
        this.flow = flow;
    }

    /**
     * @param annualLeaveContext 계산할 연차 정보를 담고 있는 객체
     * @return 산정방식(입사일)을 적용해 발생한 연차 개수
     */
    @Override
    public AnnualLeaveResult annualLeaveCalculate(AnnualLeaveContext annualLeaveContext) {
        FlowResult flowResult = flow.process(annualLeaveContext);

        return switch (flowResult.getCondition()) {
            case HD_LESS_THAN_ONE_YEAR -> {
                LessOneYearFlowResult context = (LessOneYearFlowResult) flowResult;
                MonthlyLeaveDetail monthlyLeaveDetail = monthlyAccruedLeaves(context);
                yield AnnualLeaveResult.builder()
                    .calculationType(CalculationType.HIRE_DATE)
                    .calculationDetail(monthlyLeaveDetail)
                    .explanation(LESS_THAN_ONE_YEAR)
                    .build();
            }
            case HD_LOW_AR -> {
                LowARFlowResult context = (LowARFlowResult) flowResult;
                MonthlyLeaveDetail monthlyLeaveDetail = monthlyAccruedLeaves(context);
                yield AnnualLeaveResult.builder()
                    .calculationType(CalculationType.HIRE_DATE)
                    .calculationDetail(monthlyLeaveDetail)
                    .explanation(LESS_THAN_ONE_YEAR)
                    .build();
            }
            case HD_LOW_PWR -> {
                LowPWRFlowResult context = (LowPWRFlowResult) flowResult;
                AdjustedAnnualLeaveResult adjustedLeaveDays = calculateAdjustedAnnualLeave(context);
                yield AnnualLeaveResult.builder()
                    .calculationType(CalculationType.HIRE_DATE)
                    .build();
            }
            case HD_FULL -> {
                FullFlowResult context = (FullFlowResult) flowResult;
                FullAnnualLeaveResult fullAnnualLeaveDays = calculateFullAnnualLeave(context);
                yield AnnualLeaveResult.builder()
                    .calculationType(CalculationType.HIRE_DATE)
                    .explanation(LESS_THAN_ONE_YEAR)
                    .build();
            }
        };
    }

    private AdjustedAnnualLeaveResult calculateAdjustedAnnualLeave(LowPWRFlowResult context){
        int additionalLeave = calculateAdditionalLeave(context.getServiceYears());
        double adjustedLeaveDays = formatDouble(
            (BASE_ANNUAL_LEAVE + additionalLeave) * context.getPrescribeWorkingRatio());
        return AdjustedAnnualLeaveResult.builder()
            .additionalLeave(additionalLeave)
            .adjustedLeaveDays(adjustedLeaveDays)
            .build();
    }

    private FullAnnualLeaveResult calculateFullAnnualLeave(FullFlowResult context){
        int additionalLeave = calculateAdditionalLeave(context.getServiceYears());
        return FullAnnualLeaveResult.builder()
            .additionalLeave(additionalLeave)
            .totalLeaveDays(BASE_ANNUAL_LEAVE + additionalLeave)
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
