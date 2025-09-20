package com.lawding.leavecalc.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lawding.leavecalc.domain.AnnualLeaveContext;
import com.lawding.leavecalc.dto.AnnualLeaveResponse;
import com.lawding.leavecalc.dto.AnnualLeaveResult;
import com.lawding.leavecalc.dto.request.AnnualLeaveRequest;
import com.lawding.leavecalc.mapper.AnnualLeaveMapper;
import com.lawding.leavecalc.repository.DailyUserJdbcRepository;
import com.lawding.leavecalc.service.DailyUserService;
import com.lawding.leavecalc.strategy.CalculationStrategy;
import com.lawding.leavecalc.strategy.factory.CalculationStrategyFactory;
import com.lawding.leavecalc.util.AnnualLeaveRequestValidator;
import com.lawding.leavecalc.util.AnnualLeaveRequestValidator.HeaderInfo;
import com.lawding.leavecalc.util.LogUtil;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;

public class AnnualLeaveCalculatorLambdaHandler implements
    RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final Logger logger = LogUtil.getLogger(
        AnnualLeaveCalculatorLambdaHandler.class);
    private final ObjectMapper mapper;
//    private static final DailyUserService dailyUserService =
//        new DailyUserService(new DailyUserJdbcRepository());

    public AnnualLeaveCalculatorLambdaHandler() {
        this.mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input,
        Context context) {
        String requestId = context.getAwsRequestId();
        String calculationId = UUID.randomUUID().toString();

        LogUtil.setupLogging(requestId);
        logger.info("연차계산 요청 시작 : requestId={}", requestId);

        long startTime = LogUtil.startTimer();

        try {

            HeaderInfo headerInfo = AnnualLeaveRequestValidator.validate(input.getHeaders());
            String platform = headerInfo.platform();
            boolean testMode = headerInfo.testMode();

            logger.info("헤더 검증 완료: X-Platform={}, testMode={}", platform, testMode);
            String requestBody = input.getBody();

//            dailyUserService.recordUser(platform, testMode);
            logger.info("플랫폼 당 요청 수 카운트 반영: date={}, platform={}, testMode={}", LocalDate.now(),
                platform, testMode);

            logger.debug("요청 객체 변환 시작");
            AnnualLeaveRequest request = mapper.readValue(requestBody, AnnualLeaveRequest.class);
            logger.debug("요청 객체 변환 완료: {}", request);

            logger.debug("유효성 검증 시작");
            AnnualLeaveRequestValidator.validate(request);
            logger.debug("유효성 검증 완료");

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

            AnnualLeaveResponse response = AnnualLeaveResponse.of(result, calculationId);
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
