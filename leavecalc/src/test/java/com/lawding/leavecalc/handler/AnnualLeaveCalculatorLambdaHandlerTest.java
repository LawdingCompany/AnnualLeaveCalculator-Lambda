package com.lawding.leavecalc.handler;

import com.lawding.leavecalc.domain.CalculationType;
import com.lawding.leavecalc.dto.AnnualLeaveRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.lawding.leavecalc.dto.AnnualLeaveResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AnnualLeaveCalculatorLambdaHandlerTest {

    @Test
    void shouldReturnCorrectResponse_WhenValidRequest() {
        // given

//        Context fakeContext = new FakeLambdaContext();
//
//        AnnualLeaveCalculatorLambdaHandler handler = new AnnualLeaveCalculatorLambdaHandler();
//
//        // when
//        AnnualLeaveResponse response = handler.handleRequest(request, fakeContext);
//
//        // then
//        assertNotNull(response);
//        assertTrue(response.totalLeave() > 0);
    }
}
