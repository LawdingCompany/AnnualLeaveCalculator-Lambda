package com.lawding.leavecalc.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class LogUtil {

    /**
     * 요청 시작 시 로깅 설정 초기화
     * @param requestId 요청 ID
     */
    public static void setupLogging(String requestId){
        MDC.put("requestId",requestId);
    }

    /**
     * 요청 종료 시 로깅 리소스 정리
     */
    public static void clearLogging(){
        MDC.clear();
    }

    /**
     * 클래스에 대한 로거 인스턴스 생성
     * @param clazz 로거를 생성할 클래스
     * @return 로거 인스턴스
     */
    public static Logger getLogger(Class<?> clazz){
        return LoggerFactory.getLogger(clazz);
    }

    /**
     * 성능 측정 시작
     * @return 측정 시작 시간 (밀리초)
     */
    public static long startTimer() {
        return System.currentTimeMillis();
    }

    /**
     * 성능 측정 종료 및 로깅
     * @param logger 로거 인스턴스
     * @param startTime 측정 시작 시간
     * @param operation 측정 중인 작업명
     */
    public static void logExecutionTime(Logger logger, long startTime, String operation) {
        long endTime = System.currentTimeMillis();
        logger.info("실행시간[{}]: {}ms", operation, (endTime - startTime));
    }

}
