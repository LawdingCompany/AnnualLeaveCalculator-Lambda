package com.lawding.leavecalc.domain;

import com.lawding.leavecalc.common.DateRange;
import com.lawding.leavecalc.type.CalculationType;
import java.time.LocalDate;
import java.util.List;

/**
 * 연차 계산에 필요한 정보를 담은 객체입니다.
 * <p>
 *
 * @param calculationType       연차 산정 방식 (입사일, 회계연도)
 * @param hireDate              입사일
 * @param referenceDate         기준일(연차 산정 기준일)
 * @param hasExcludedWorkPeriod 근무 제외 기간 여부
 * @param excludedWorkPeriod    근무 제외 기간
 */
public record AnnualLeaveContext(
    CalculationType calculationType,
    LocalDate hireDate,
    LocalDate referenceDate,
    boolean hasExcludedWorkPeriod,
    List<DateRange> excludedWorkPeriod
) {

}
