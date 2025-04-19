package com.lawding.leavecalc.service;

import com.lawding.leavecalc.strategy.CalculationStrategy;
import java.time.LocalDate;
import java.time.Period;


public class AnnualLeaveCalculator {

    /*
             @Param calculationMethod : 연차유급휴가 산정 방식(입사일 or 회계연도)
             @Param fiscalStartStr : 회계연도
             @Param hireDate : 입사일
             @Param calcDate : 연차 산정 기준 날짜(특정 날짜를 기준으로 발생한 연차)
             @Param extraInput : 비근무 기간 있는지 여부(예/아니오만 가능)
             @Param extraPeriods : 비근무 기간 있을 시 저장하는 배열
             @Param annualCalcPeriod : 연차 산정 단위 기간 배열
             @Method getChoice() : 프롬프트창 출력
             @Method getDate() : String -> LocalDate 형식
             @Method countWeekdays() : 평일 수 세기
     */
}
