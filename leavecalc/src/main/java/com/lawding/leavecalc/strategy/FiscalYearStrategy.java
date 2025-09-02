package com.lawding.leavecalc.strategy;


import com.lawding.leavecalc.domain.AnnualLeaveContext;
import com.lawding.leavecalc.domain.flow.FlowResult;
import com.lawding.leavecalc.dto.AnnualLeaveResult;
import com.lawding.leavecalc.exception.AnnualLeaveException;
import com.lawding.leavecalc.exception.ErrorCode;
import com.lawding.leavecalc.flow.CalculationFlow;

public final class FiscalYearStrategy implements CalculationStrategy {

    private final CalculationFlow flow;

    public FiscalYearStrategy(CalculationFlow flow) {
        this.flow = flow;
    }

    /**
     * @param annualLeaveContext 계산할 연차 정보를 담은 객체
     * @return 산정방식(회계연도)을 적용해 발생한 연차 개수
     * <p>
     */
    @Override
    public AnnualLeaveResult annualLeaveCalculate(AnnualLeaveContext annualLeaveContext) {
        FlowResult flowResult = flow.process(annualLeaveContext);
        return null;
    }

}
