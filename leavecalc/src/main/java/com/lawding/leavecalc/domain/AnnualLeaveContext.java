package com.lawding.leavecalc.domain;

import java.time.LocalDate;
import java.util.List;
import lombok.Builder;

/**
 * 연차 계산에 필요한 정보를 담은 객체입니다.
 * <p>
 *
 * @param calculationType       연차 산정 방식 (1:입사일, 2:회계연도)
 * @param fiscalYear            회계연도(nullable)
 * @param hireDate              입사일
 * @param referenceDate         기준일(연차 산정 기준일)
 * @param hasExcludedWorkPeriod 근무 제외 기간 여부
 * @param excludedWorkPeriod    근무 제외 기간
 */

@Builder
public record AnnualLeaveContext(
    CalculationType calculationType,
    LocalDate fiscalYear,
    LocalDate hireDate,
    LocalDate referenceDate,
    boolean hasExcludedWorkPeriod,
    List<DateRange> excludedWorkPeriod
) {

}
