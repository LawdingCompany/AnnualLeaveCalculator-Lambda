//package com.lawding.leavecalc.handler;
//
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//import com.amazonaws.services.lambda.runtime.Context;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.SerializationFeature;
//import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
//import com.lawding.leavecalc.constant.AnnualLeaveMessages;
//import com.lawding.leavecalc.domain.AnnualLeaveContext;
//import com.lawding.leavecalc.domain.AnnualLeaveResult;
//import com.lawding.leavecalc.domain.AnnualLeaveResultType;
//import com.lawding.leavecalc.domain.CalculationType;
//import com.lawding.leavecalc.domain.detail.CalculationDetail;
//import com.lawding.leavecalc.dto.AnnualLeaveRequest;
//import com.lawding.leavecalc.dto.AnnualLeaveResponse;
//import com.lawding.leavecalc.strategy.CalculationStrategyFactory;
//import com.lawding.leavecalc.strategy.FiscalYearStrategy;
//import com.lawding.leavecalc.strategy.HireDateStrategy;
//import java.time.LocalDate;
//import java.time.MonthDay;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.MockedStatic;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//@ExtendWith(MockitoExtension.class)
//public class AnnualLeaveCalculatorLambdaHandlerTest {
//
//    private AnnualLeaveCalculatorLambdaHandler handler;
//    private ObjectMapper objectMapper;
//
//    @Mock
//    private Context context;
//
//
//    @Mock
//    private HireDateStrategy hireDateStrategy;
//
//    @Mock
//    private FiscalYearStrategy fiscalYearStrategy;
//
//
//    @BeforeEach
//    void setUp() {
//        handler = new AnnualLeaveCalculatorLambdaHandler();
//        objectMapper = new ObjectMapper();
//        objectMapper.registerModule(new JavaTimeModule());
//        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
//    }
//
//    @Test
//    @DisplayName("입사일 방식 연차 계산 흐름 테스트")
//    void handleRequest_HireDateType_Success() {
//        // Given
//        AnnualLeaveRequest request = AnnualLeaveRequest.builder()
//            .calculationType(1)
//            .hireDate("2024-03-01")
//            .referenceDate("2024-06-03")
//            .build();
//
//        LocalDate hireDate = LocalDate.parse("2024-03-01");
//        LocalDate referenceDate = LocalDate.parse("2024-06-03");
//        CalculationDetail calculationDetail = mock(CalculationDetail.class);
//        when(calculationDetail.getTotalLeaveDays()).thenReturn(3.0);
//
//        AnnualLeaveResult mockResult = AnnualLeaveResult.builder()
//            .calculationType(CalculationType.HIRE_DATE)
//            .annualLeaveResultType(AnnualLeaveResultType.MONTHLY)
//            .hireDate(hireDate)
//            .referenceDate(referenceDate)
//            .calculationDetail(calculationDetail)
//            .explanation(AnnualLeaveMessages.LESS_THAN_ONE_YEAR)
//            .build();
//
//        try (MockedStatic<CalculationStrategyFactory> factoryMock = mockStatic(
//            CalculationStrategyFactory.class)) {
//            // 입사일 기준일 때 HireDateStrategy 전략 반환
//            factoryMock.when(() -> CalculationStrategyFactory.from(any(AnnualLeaveContext.class)))
//                .thenAnswer(invocation -> {
//                    AnnualLeaveContext context = invocation.getArgument(0);
//                    // 팩토리가 전략 패턴을 제대로 선택하는지 확인
//                    if (context.getCalculationType() == CalculationType.HIRE_DATE) {
//                        return hireDateStrategy;
//                    } else {
//                        return fiscalYearStrategy;
//                    }
//                });
//
//            when(hireDateStrategy.annualLeaveCalculate(any(AnnualLeaveContext.class))).thenReturn(
//                mockResult);
//
//            // when
//            AnnualLeaveResponse response = handler.handleRequest(request, context);
//
//            assertNotNull(response);
//            assertEquals(CalculationType.HIRE_DATE, response.calculationType());
//            assertEquals(hireDate, response.hireDate());
//            assertEquals(referenceDate, response.referenceDate());
//            assertEquals(3, response.calculationDetail().getTotalLeaveDays());
//            assertEquals(AnnualLeaveMessages.LESS_THAN_ONE_YEAR, response.explanation());
//
//            // 팩토리가 호출되었는지 확인
//            factoryMock.verify(
//                () -> CalculationStrategyFactory.from(any(AnnualLeaveContext.class)));
//
//            // 올바른 전략이 호출되었는지 확인
//            verify(hireDateStrategy).annualLeaveCalculate(any(AnnualLeaveContext.class));
//            verify(fiscalYearStrategy, never()).annualLeaveCalculate(
//                any(AnnualLeaveContext.class));
//        }
//    }
//
//    @Test
//    @DisplayName("회계연도 방식 연차 계산 흐름 테스트")
//    void handleRequest_FiscalYearDateType_Success() {
//        // Given
//        AnnualLeaveRequest request = AnnualLeaveRequest.builder()
//            .calculationType(2)
//            .fiscalYear("01-01")
//            .hireDate("2024-03-01")
//            .referenceDate("2024-06-03")
//            .build();
//
//        LocalDate hireDate = LocalDate.parse("2024-03-01");
//        LocalDate referenceDate = LocalDate.parse("2024-06-03");
//        MonthDay fiscalYear = MonthDay.of(1, 1);
//        CalculationDetail calculationDetail = mock(CalculationDetail.class);
//        when(calculationDetail.getTotalLeaveDays()).thenReturn(3.0);
//
//        AnnualLeaveResult mockResult = AnnualLeaveResult.builder()
//            .calculationType(CalculationType.FISCAL_YEAR)
//            .annualLeaveResultType(AnnualLeaveResultType.MONTHLY)
//            .fiscalYear(fiscalYear)
//            .hireDate(hireDate)
//            .referenceDate(referenceDate)
//            .calculationDetail(calculationDetail)
//            .explanation(AnnualLeaveMessages.LESS_THAN_ONE_YEAR)
//            .build();
//
//        try (MockedStatic<CalculationStrategyFactory> factoryMock = mockStatic(
//            CalculationStrategyFactory.class)) {
//            // 입사일 기준일 때 HireDateStrategy 전략 반환
//            factoryMock.when(() -> CalculationStrategyFactory.from(any(AnnualLeaveContext.class)))
//                .thenAnswer(invocation -> {
//                    AnnualLeaveContext context = invocation.getArgument(0);
//                    // 팩토리가 전략 패턴을 제대로 선택하는지 확인
//                    if (context.getCalculationType() == CalculationType.HIRE_DATE) {
//                        return hireDateStrategy;
//                    } else {
//                        return fiscalYearStrategy;
//                    }
//                });
//
//            when(fiscalYearStrategy.annualLeaveCalculate(any(AnnualLeaveContext.class))).thenReturn(
//                mockResult);
//
//            // when
//            AnnualLeaveResponse response = handler.handleRequest(request, context);
//
//            assertNotNull(response);
//            assertEquals(CalculationType.FISCAL_YEAR, response.calculationType());
//            assertEquals(fiscalYear, response.fiscalYear());
//            assertEquals(hireDate, response.hireDate());
//            assertEquals(referenceDate, response.referenceDate());
//            assertEquals(3, response.calculationDetail().getTotalLeaveDays());
//            assertEquals(AnnualLeaveMessages.LESS_THAN_ONE_YEAR, response.explanation());
//
//            // 팩토리가 호출되었는지 확인
//            factoryMock.verify(
//                () -> CalculationStrategyFactory.from(any(AnnualLeaveContext.class)));
//
//            // 올바른 전략이 호출되었는지 확인
//            verify(fiscalYearStrategy).annualLeaveCalculate(any(AnnualLeaveContext.class));
//            verify(hireDateStrategy, never()).annualLeaveCalculate(
//                any(AnnualLeaveContext.class));
//        }
//    }
//
//
//}
