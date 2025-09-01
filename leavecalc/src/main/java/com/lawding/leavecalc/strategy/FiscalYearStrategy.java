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
        return switch (flowResult.getFlowStep()) {
            case FY_BEFORE_PRORATED -> null;
            case FY_MONTHLY_AND_PRORATED -> null;
            case FY_PRORATED_AND_UNDER_AR -> null;
            case FY_PRORATED_FULL -> null;
            case FY_AFTER_FIRST_REGULAR_START_DATE_AND_UNDER_AR -> null;
            case FY_AFTER_FIRST_REGULAR_START_DATE_AND_OVER_AR_AND_UNDER_PWR -> null;
            case FY_FULL -> null;
            default -> throw new AnnualLeaveException(ErrorCode.FISCALYEAR_FLOW_ERROR);
        };
    }

}
