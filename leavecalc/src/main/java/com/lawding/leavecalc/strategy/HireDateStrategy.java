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


}
