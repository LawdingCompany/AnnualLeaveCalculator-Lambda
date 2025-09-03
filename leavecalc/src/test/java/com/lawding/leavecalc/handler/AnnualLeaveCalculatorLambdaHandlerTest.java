package com.lawding.leavecalc.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lawding.leavecalc.domain.AnnualLeaveContext;
import com.lawding.leavecalc.dto.AnnualLeaveResult;
import com.lawding.leavecalc.dto.request.AnnualLeaveRequest;
import com.lawding.leavecalc.dto.response.AnnualLeaveResponse;
import com.lawding.leavecalc.mapper.AnnualLeaveMapper;
import com.lawding.leavecalc.strategy.factory.CalculationStrategyFactory;
import com.lawding.leavecalc.strategy.FiscalYearStrategy;
import com.lawding.leavecalc.strategy.HireDateStrategy;
import com.lawding.leavecalc.util.AnnualLeaveRequestValidator;

import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AnnualLeaveCalculatorLambdaHandlerTest {

    private AnnualLeaveCalculatorLambdaHandler handler;
    private ObjectMapper objectMapper;

    @Mock
    private Context context;

    @Mock
    private HireDateStrategy hireDateStrategy;

    @Mock
    private FiscalYearStrategy fiscalYearStrategy;

    @Mock
    private AnnualLeaveResult mockResult;

    @Mock
    private AnnualLeaveContext mockContext;

    @BeforeEach
    void setUp() {
        handler = new AnnualLeaveCalculatorLambdaHandler();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    @DisplayName("입사일 방식 연차 계산 - 모든 단계가 순차적으로 호출되는지 확인")
    void handleRequest_HireDateType_VerifyAllStepsCalled() throws IOException {
        // Given
        AnnualLeaveRequest request = AnnualLeaveRequest.builder()
            .calculationType(1) // HIRE_DATE
            .hireDate("2024-03-01")
            .referenceDate("2024-06-03")
            .build();

        String requestJson = objectMapper.writeValueAsString(request);
        APIGatewayProxyRequestEvent apiGatewayEvent = new APIGatewayProxyRequestEvent();
        apiGatewayEvent.setBody(requestJson);

        // 모든 정적 메서드 모킹
        try (MockedStatic<AnnualLeaveRequestValidator> validatorMock = mockStatic(AnnualLeaveRequestValidator.class);
            MockedStatic<AnnualLeaveMapper> mapperMock = mockStatic(AnnualLeaveMapper.class);
            MockedStatic<CalculationStrategyFactory> factoryMock = mockStatic(CalculationStrategyFactory.class);
            MockedStatic<AnnualLeaveResponse> responseMock = mockStatic(AnnualLeaveResponse.class)) {

            // Step 1: 유효성 검사 모킹 (아무것도 하지 않음 - void 메서드)
            validatorMock.when(() -> AnnualLeaveRequestValidator.validate(any(AnnualLeaveRequest.class)))
                .thenAnswer(invocation -> null);

            // Step 2: Mapper 모킹
            mapperMock.when(() -> AnnualLeaveMapper.toContext(any(AnnualLeaveRequest.class)))
                .thenReturn(mockContext);

            // Step 3: Factory 모킹 - 직접 전략을 반환하도록 설정
            factoryMock.when(() -> CalculationStrategyFactory.from(any(AnnualLeaveContext.class)))
                .thenReturn(hireDateStrategy);

            // Step 4: Strategy 모킹
            when(hireDateStrategy.annualLeaveCalculate(any(AnnualLeaveContext.class)))
                .thenReturn(mockResult);

            // Step 5: Response.of 모킹
            AnnualLeaveResponse mockResponse = mock(AnnualLeaveResponse.class);
            responseMock.when(() -> AnnualLeaveResponse.of(any(AnnualLeaveResult.class)))
                .thenReturn(mockResponse);


            // When
            APIGatewayProxyResponseEvent response = handler.handleRequest(apiGatewayEvent, context);

            // Then: 각 단계가 호출되었는지 검증

            // 응답 검증
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());

            // 유효성 검사가 호출되었는지 확인
            validatorMock.verify(() -> AnnualLeaveRequestValidator.validate(any(AnnualLeaveRequest.class)));

            // 매퍼가 호출되었는지 확인
            mapperMock.verify(() -> AnnualLeaveMapper.toContext(any(AnnualLeaveRequest.class)));

            // 팩토리가 호출되었는지 확인
            factoryMock.verify(() -> CalculationStrategyFactory.from(any(AnnualLeaveContext.class)));

            // 올바른 전략이 호출되었는지 확인
            verify(hireDateStrategy).annualLeaveCalculate(any(AnnualLeaveContext.class));
            verify(fiscalYearStrategy, never()).annualLeaveCalculate(any(AnnualLeaveContext.class));

            // 응답 생성 메서드가 호출되었는지 확인
            responseMock.verify(() -> AnnualLeaveResponse.of(any(AnnualLeaveResult.class)));
        }
    }

    @Test
    @DisplayName("회계연도 방식 연차 계산 - 모든 단계가 순차적으로 호출되는지 확인")
    void handleRequest_FiscalYearType_VerifyAllStepsCalled() throws IOException {
        // Given
        AnnualLeaveRequest request = AnnualLeaveRequest.builder()
            .calculationType(2) // FISCAL_YEAR
            .fiscalYear("01-01")
            .hireDate("2024-03-01")
            .referenceDate("2024-06-03")
            .build();

        // 요청을 JSON으로 변환하여 InputStream 생성
        String requestJson = objectMapper.writeValueAsString(request);
        APIGatewayProxyRequestEvent apiGatewayEvent = new APIGatewayProxyRequestEvent();
        apiGatewayEvent.setBody(requestJson);

        // 모든 정적 메서드 모킹
        try (MockedStatic<AnnualLeaveRequestValidator> validatorMock = mockStatic(AnnualLeaveRequestValidator.class);
            MockedStatic<AnnualLeaveMapper> mapperMock = mockStatic(AnnualLeaveMapper.class);
            MockedStatic<CalculationStrategyFactory> factoryMock = mockStatic(CalculationStrategyFactory.class);
            MockedStatic<AnnualLeaveResponse> responseMock = mockStatic(AnnualLeaveResponse.class)) {

            // Step 1: 유효성 검사 모킹 (아무것도 하지 않음 - void 메서드)
            validatorMock.when(() -> AnnualLeaveRequestValidator.validate(any(AnnualLeaveRequest.class)))
                .thenAnswer(invocation -> null);

            // Step 2: Mapper 모킹
            mapperMock.when(() -> AnnualLeaveMapper.toContext(any(AnnualLeaveRequest.class)))
                .thenReturn(mockContext);

            // Step 3: Factory 모킹 - 직접 회계연도 전략을 반환하도록 설정
            factoryMock.when(() -> CalculationStrategyFactory.from(any(AnnualLeaveContext.class)))
                .thenReturn(fiscalYearStrategy);

            // Step 4: Strategy 모킹
            when(fiscalYearStrategy.annualLeaveCalculate(any(AnnualLeaveContext.class)))
                .thenReturn(mockResult);

            // Step 5: Response.of 모킹
            AnnualLeaveResponse mockResponse = mock(AnnualLeaveResponse.class);
            responseMock.when(() -> AnnualLeaveResponse.of(any(AnnualLeaveResult.class)))
                .thenReturn(mockResponse);

            // When
            APIGatewayProxyResponseEvent response = handler.handleRequest(apiGatewayEvent, context);

            assertNotNull(response);
            assertEquals(200, response.getStatusCode());

            // 유효성 검사가 호출되었는지 확인
            validatorMock.verify(() -> AnnualLeaveRequestValidator.validate(any(AnnualLeaveRequest.class)));

            // 매퍼가 호출되었는지 확인
            mapperMock.verify(() -> AnnualLeaveMapper.toContext(any(AnnualLeaveRequest.class)));

            // 팩토리가 호출되었는지 확인
            factoryMock.verify(() -> CalculationStrategyFactory.from(any(AnnualLeaveContext.class)));

            // 올바른 전략이 호출되었는지 확인
            verify(fiscalYearStrategy).annualLeaveCalculate(any(AnnualLeaveContext.class));
            verify(hireDateStrategy, never()).annualLeaveCalculate(any(AnnualLeaveContext.class));

            // 응답 생성 메서드가 호출되었는지 확인
            responseMock.verify(() -> AnnualLeaveResponse.of(any(AnnualLeaveResult.class)));
        }
    }
}