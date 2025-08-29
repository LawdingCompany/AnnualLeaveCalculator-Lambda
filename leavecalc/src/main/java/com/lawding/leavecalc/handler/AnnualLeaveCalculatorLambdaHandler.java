package com.lawding.leavecalc.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lawding.leavecalc.dto.request.AnnualLeaveRequest;
import com.lawding.leavecalc.dto.response.AnnualLeaveResponse;
import com.lawding.leavecalc.domain.AnnualLeaveContext;
import com.lawding.leavecalc.domain.AnnualLeaveResult;
import com.lawding.leavecalc.mapper.AnnualLeaveMapper;
import com.lawding.leavecalc.strategy.CalculationStrategy;
import com.lawding.leavecalc.strategy.factory.CalculationStrategyFactory;
import com.lawding.leavecalc.util.AnnualLeaveRequestValidator;
import com.lawding.leavecalc.util.LogUtil;
import java.util.Map;
import org.slf4j.Logger;

public class AnnualLeaveCalculatorLambdaHandler implements
    RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final Logger logger = LogUtil.getLogger(
        AnnualLeaveCalculatorLambdaHandler.class);
    private final ObjectMapper mapper;

    public AnnualLeaveCalculatorLambdaHandler() {
        this.mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input,
        Context context) {
        String requestId = context.getAwsRequestId();
        LogUtil.setupLogging(requestId);
        logger.info("연차계산 요청 시작 : requestId={}", requestId);

        long startTime = LogUtil.startTimer();

        try {
            String requestBody = input.getBody();

            logger.debug("요청 객체 변환 시작");
            AnnualLeaveRequest request = mapper.readValue(requestBody, AnnualLeaveRequest.class);
            logger.debug("요청 객체 변환 완료: {}", request);

            logger.info("유효성 검증 시작");
            AnnualLeaveRequestValidator.validate(request);
            logger.info("유효성 검증 완료");

            logger.debug("컨텍스트 변환 시작");
            AnnualLeaveContext annualLeaveContext = AnnualLeaveMapper.toContext(request);
            logger.debug("컨텍스트 변환 완료: {}", annualLeaveContext);

            logger.info("계산 전략 결정 시작");
            CalculationStrategy calculationStrategy = CalculationStrategyFactory.from(
                annualLeaveContext);
            logger.info("계산 전략 결정 완료: {}", calculationStrategy.getClass().getSimpleName());

            logger.info("연차 계산 시작");
            AnnualLeaveResult result = calculationStrategy.annualLeaveCalculate(annualLeaveContext);
            logger.info("연차 계산 완료: {}", result);

            AnnualLeaveResponse response = AnnualLeaveResponse.of(result);
            logger.info("응답 객체 생성 완료: {}", response);

            String responseBody = mapper.writeValueAsString(response);
            logger.debug("응답 JSON 변환 완료: {}", responseBody);

            LogUtil.logExecutionTime(logger, startTime, "연차계산-전체");

            logger.info("연차계산 요청 처리 성공");

            return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withHeaders(Map.of("Content-Type", "application/json"))
                .withBody(responseBody);
        } catch (Exception e) {

            logger.error("연차계산 중 오류 발생: {}", e.getMessage(), e);

            return new APIGatewayProxyResponseEvent()
                .withStatusCode(400)
                .withBody(e.getMessage());
        } finally {
            logger.info("연차계산 요청 종료: requestId={}", requestId);
            LogUtil.clearLogging();
        }
    }
}
