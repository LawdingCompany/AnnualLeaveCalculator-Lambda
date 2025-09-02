package com.lawding.leavecalc.strategy;

import static com.lawding.leavecalc.util.AnnualLeaveHelper.*;
import static com.lawding.leavecalc.constant.AnnualLeaveConstants.*;

import com.lawding.leavecalc.calculator.dispatcher.CalculationDispatcher;
import com.lawding.leavecalc.domain.AnnualLeaveContext;
import com.lawding.leavecalc.dto.AdjustedAnnualLeaveResult;
import com.lawding.leavecalc.dto.FullAnnualLeaveResult;
import com.lawding.leavecalc.dto.detail.CalculationDetail;
import com.lawding.leavecalc.dto.AnnualLeaveResult;
import com.lawding.leavecalc.domain.flow.FlowResult;
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
        CalculationDetail calculationDetail = CalculationDispatcher.calculate(flowResult);
        return null;
    }




}
