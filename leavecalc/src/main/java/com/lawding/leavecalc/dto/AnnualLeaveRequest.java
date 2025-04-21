package com.lawding.leavecalc.dto;

import com.lawding.leavecalc.domain.DateRange;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

/**
 * 연차 계산 요청을 위한 DTO 클래스입니다.
 * <p>
 * 사용자는 연차 산정 방식에 따라 입사일 기준 또는 회계연도 기준으로 계산 요청을 할 수 있으며,
 * 선택한 산정 방식에 따라 필요한 값이 달라질 수 있습니다.
 *
 * <ul>
 *     <li><b>calculationType</b>: 연차 산정 방식 코드 (1: 입사일 기준, 2: 회계연도 기준)</li>
 *     <li><b>hireDate</b>: 입사일 (예: "2023-01-01")</li>
 *     <li><b>referenceDate</b>: 연차 계산 기준일</li>
 *     <li><b>fiscalYearStartDate</b>: 회계연도 기준일 (calculationType이 2일 경우 필요)</li>
 *     <li><b>hasExcludedWorkPeriod</b>: 근무 제외 기간 존재 여부</li>
 *     <li><b>excludedWorkPeriod</b>: 근무 제외 기간 리스트 (DateRange 객체 리스트)</li>
 * </ul>
 *
 * @author jaeyun
 * @see com.lawding.leavecalc.domain.DateRange
 */

@Getter
@Builder
public class AnnualLeaveRequest{
    private int calculationType;
    private String fiscalYear; // nullable
    private String hireDate;
    private String referenceDate;
    private boolean hasExcludedWorkPeriod;
    private List<DateRange> excludedWorkPeriod; // nullable
}
