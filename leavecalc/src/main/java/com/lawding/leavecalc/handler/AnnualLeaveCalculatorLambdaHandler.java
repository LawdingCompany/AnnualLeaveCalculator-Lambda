package com.lawding.leavecalc.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.lawding.leavecalc.domain.AnnualLeaveContext;
import com.lawding.leavecalc.domain.result.AnnualLeaveResult;
import com.lawding.leavecalc.dto.AnnualLeaveRequest;
import com.lawding.leavecalc.dto.AnnualLeaveResponse;
import com.lawding.leavecalc.mapper.AnnualLeaveMapper;
import com.lawding.leavecalc.strategy.CalculationStrategy;
import com.lawding.leavecalc.strategy.CalculationStrategyFactory;
import com.lawding.leavecalc.util.AnnualLeaveRequestValidator;

public class AnnualLeaveCalculatorLambdaHandler implements
    RequestHandler<AnnualLeaveRequest, AnnualLeaveResponse> {

    @Override
    public AnnualLeaveResponse handleRequest(AnnualLeaveRequest request, Context context) {
        AnnualLeaveRequestValidator.validate(request);
        AnnualLeaveContext annualLeaveContext = AnnualLeaveMapper.toContext(request);
        CalculationStrategy calculationStrategy = CalculationStrategyFactory.from(
            annualLeaveContext);
        AnnualLeaveResult result = calculationStrategy.annualLeaveCalculate(
            annualLeaveContext);
        return AnnualLeaveResponse.of(result);
    }
}


