package com.lawding.leavecalc.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lawding.leavecalc.dto.AnnualLeaveRequest;
import com.lawding.leavecalc.dto.AnnualLeaveResponse;
import com.lawding.leavecalc.domain.AnnualLeaveContext;
import com.lawding.leavecalc.domain.AnnualLeaveResult;
import com.lawding.leavecalc.mapper.AnnualLeaveMapper;
import com.lawding.leavecalc.strategy.CalculationStrategy;
import com.lawding.leavecalc.strategy.CalculationStrategyFactory;
import com.lawding.leavecalc.util.AnnualLeaveRequestValidator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AnnualLeaveCalculatorLambdaHandler implements RequestStreamHandler {

    private final ObjectMapper mapper;

    public AnnualLeaveCalculatorLambdaHandler() {
        this.mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context)
        throws IOException {
        AnnualLeaveRequest request = mapper.readValue(input, AnnualLeaveRequest.class);
        AnnualLeaveRequestValidator.validate(request);
        AnnualLeaveContext annualLeaveContext = AnnualLeaveMapper.toContext(request);
        CalculationStrategy calculationStrategy = CalculationStrategyFactory.from(
            annualLeaveContext);
        AnnualLeaveResult result = calculationStrategy.annualLeaveCalculate(annualLeaveContext);
        AnnualLeaveResponse response = AnnualLeaveResponse.of(result);
        mapper.writeValue(output, response);
    }
}
