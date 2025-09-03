package com.lawding.leavecalc.strategy;


import static com.lawding.leavecalc.domain.resolver.ExplanationResolver.extractPWR;
import static com.lawding.leavecalc.domain.resolver.ExplanationResolver.resolveAll;
import static com.lawding.leavecalc.mapper.AnnualLeaveMapper.toDtoList;
import static com.lawding.leavecalc.mapper.AnnualLeaveMapper.toStringList;

import com.lawding.leavecalc.calculator.dispatcher.CalculationDispatcher;
import com.lawding.leavecalc.domain.AnnualLeaveContext;
import com.lawding.leavecalc.domain.flow.FlowResult;
import com.lawding.leavecalc.domain.resolver.ExplanationResolver;
import com.lawding.leavecalc.dto.AnnualLeaveResult;
import com.lawding.leavecalc.dto.detail.CalculationDetail;
import com.lawding.leavecalc.dto.request.NonWorkingPeriodDto;
import com.lawding.leavecalc.exception.AnnualLeaveException;
import com.lawding.leavecalc.exception.ErrorCode;
import com.lawding.leavecalc.flow.CalculationFlow;
import java.util.List;

public final class FiscalYearStrategy implements CalculationStrategy {

    private final CalculationFlow flow;

    public FiscalYearStrategy(CalculationFlow flow) {
        this.flow = flow;
    }

    /**
     * @param context 계산할 연차 정보를 담은 객체
     * @return 산정방식(회계연도)을 적용해 발생한 연차 개수
     * <p>
     */
    @Override
    public AnnualLeaveResult annualLeaveCalculate(AnnualLeaveContext context) {
        FlowResult flowResult = flow.process(context);
        CalculationDetail calculationDetail = CalculationDispatcher.calculate(flowResult);

        List<NonWorkingPeriodDto> nonWorkingPeriod = toDtoList(context.getNonWorkingPeriods());

        List<String> companyHolidays = toStringList(context.getCompanyHolidays());

        List<String> explanations = resolveAll(flowResult.getSteps(), context.getCalculationType());

        int serviceYears = flowResult.getContext().getServiceYears();
        Double prescribedWorkingRatio = extractPWR(flowResult.getContext()); // null 허용
        List<String> nonWorkingExplanations =
            ExplanationResolver.resolveNonWorkingExplanations(nonWorkingPeriod, serviceYears,
                prescribedWorkingRatio);

        return AnnualLeaveResult.builder()
            .calculationType(context.getCalculationType().name())
            .hireDate(context.getHireDate().toString())
            .referenceDate(context.getReferenceDate().toString())
            .nonWorkingPeriod(nonWorkingPeriod)
            .companyHolidays(companyHolidays)
            .leaveType(flowResult.getLeaveType().name())
            .calculationDetail(calculationDetail)
            .explanations(explanations)
            .nonWorkingExplanations(nonWorkingExplanations)
            .build();
    }

}
