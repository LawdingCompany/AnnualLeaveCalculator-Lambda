package com.lawding.leavecalc.strategy;

import com.lawding.leavecalc.domain.AnnualLeaveContext;
import com.lawding.leavecalc.domain.AnnualLeaveResult;


/**
 * 연차 계산에 필요한 정보를 담은 객체입니다.
 * <p>
 *
 * @param hireDate              입사일
 * @param referenceDate         기준일(연차 산정 기준일)
 * @param hasExcludedWorkPeriod 근무 제외 기간 여부
 * @param excludedWorkPeriod    근무 제외 기간
 *
 *
 */
public final class HireDateStrategy implements CalculationStrategy{

    /**
     *
     * @param annualLeaveContext
     * @return
     *
     * I) 입사일 방식
     *             - 입사일이 1년 미만일 경우 => 연차 산정 단위 기간 = [입사일, 기준일]
     *             n = 기준일과 입사일 사이의 총 기간의 연수 = 근속년수?
     *             - 입사일이 1년 이상일 경우 => 연차 산정 단위 기간 = [입사일 + (근속년수 - 1), 입사일 + 근속년수 - 1일]
     *             Q. 기간 마지막 날 = (기준년도 입사일 - 1일) 해도 되는건가? No
     *             예시) 입사일 : 2020-03-09 , 기준연도 2024-02-25 => n = 3, [2022-03-09, 2023-03-08] 이 맞나? 나머지 2년은 어디감?
     *             n년 산정 방식 과 마지막 1년(~기준 날짜)까지 산정 방식이 다른가? n년은 신경 안쓰나? 어차피 1년 안에 무조건 다써야하나?
     *
     *             Q. 입사일 2023-03-21 / 기준일 2024-03-20,21,22 인 경우? 혹은 한달내(2.22 ~ 3.20)까지 같은 처리?
     */
    @Override
    public AnnualLeaveResult annualLeaveCalculate(AnnualLeaveContext annualLeaveContext) {
        return null;
    }
}
