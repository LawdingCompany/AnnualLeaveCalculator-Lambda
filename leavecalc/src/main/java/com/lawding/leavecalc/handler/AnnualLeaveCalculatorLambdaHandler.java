package com.lawding.leavecalc.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.lawding.leavecalc.domain.AnnualLeaveContext;
import com.lawding.leavecalc.dto.AnnualLeaveRequest;
import com.lawding.leavecalc.dto.AnnualLeaveResponse;
import com.lawding.leavecalc.mapper.AnnualLeaveMapper;

public class AnnualLeaveCalculatorLambdaHandler implements
    RequestHandler<AnnualLeaveRequest, AnnualLeaveResponse> {

    @Override
    public AnnualLeaveResponse handleRequest(AnnualLeaveRequest request, Context context) {
     //   AnnualLeaveContext annualLeaveContext = AnnualLeaveMapper.toContext(request);
        return null;
    }
}


