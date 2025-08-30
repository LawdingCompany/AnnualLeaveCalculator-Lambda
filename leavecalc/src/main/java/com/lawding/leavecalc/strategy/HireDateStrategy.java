package com.lawding.leavecalc.strategy;

import static com.lawding.leavecalc.constant.AnnualLeaveMessages.*;
import static com.lawding.leavecalc.util.AnnualLeaveHelper.*;
import static com.lawding.leavecalc.constant.AnnualLeaveConstants.*;

import com.lawding.leavecalc.domain.AnnualLeaveContext;
import com.lawding.leavecalc.domain.CalculationType;
import com.lawding.leavecalc.domain.flow.hiredate.FullFlowResult;
import com.lawding.leavecalc.dto.AdjustedAnnualLeaveResult;
import com.lawding.leavecalc.dto.FullAnnualLeaveResult;
import com.lawding.leavecalc.dto.detail.MonthlyLeaveDetail;
import com.lawding.leavecalc.dto.AnnualLeaveResult;
import com.lawding.leavecalc.domain.flow.FlowResult;
import com.lawding.leavecalc.domain.flow.hiredate.UnderPWRFlowResult;
import com.lawding.leavecalc.domain.flow.hiredate.LessOneYearFlowResult;
import com.lawding.leavecalc.domain.flow.hiredate.UnderARFlowResult;
import com.lawding.leavecalc.exception.AnnualLeaveException;
import com.lawding.leavecalc.exception.ErrorCode;
import com.lawding.leavecalc.flow.CalculationFlow;


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
            case HD_LESS_ONE_YEAR -> {
                LessOneYearFlowResult context = (LessOneYearFlowResult) flowResult;
                MonthlyLeaveDetail monthlyLeaveDetail = monthlyAccruedLeaves(context);
                yield AnnualLeaveResult.builder()
                    .calculationType(CalculationType.HIRE_DATE)
                    .calculationDetail(monthlyLeaveDetail)
                    .explanation(LESS_THAN_ONE_YEAR)
                    .build();
            }
            case HD_AFTER_ONE_YEAR_AND_UNDER_AR -> {
                UnderARFlowResult context = (UnderARFlowResult) flowResult;
                MonthlyLeaveDetail monthlyLeaveDetail = monthlyAccruedLeaves(context);
                yield AnnualLeaveResult.builder()
                    .calculationType(CalculationType.HIRE_DATE)
                    .calculationDetail(monthlyLeaveDetail)
                    .explanation(LESS_THAN_ONE_YEAR)
                    .build();
            }
            case HD_AFTER_ONE_YEAR_AND_OVER_AR_AND_UNDER_PWR -> {
                UnderPWRFlowResult context = (UnderPWRFlowResult) flowResult;
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
            default -> throw new AnnualLeaveException(ErrorCode.HIREDATE_FLOW_ERROR);
        };
    }

    private AdjustedAnnualLeaveResult calculateAdjustedAnnualLeave(UnderPWRFlowResult context){
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
