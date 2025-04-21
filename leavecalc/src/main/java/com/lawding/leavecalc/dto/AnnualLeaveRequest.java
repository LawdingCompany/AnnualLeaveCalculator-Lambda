package com.lawding.leavecalc.dto;

import com.lawding.leavecalc.domain.DateRange;
import java.util.List;
import lombok.Getter;

/**
 * 연차 계산을 위한 API 요청 객체입니다.
 * <p>
 * 사용자는 연차 산정 방식에 따라 입사일 기준 또는 회계연도 기준으로 계산을 요청할 수 있으며,
 * 필요한 입력 값들은 아래와 같습니다.
 *
 * <ul>
 *     <li><b>calculationType</b> - 연차 산정 방식 (1: 입사일 기준, 2: 회계연도 기준)</li>
 *     <li><b>hireDate</b> - 입사일 (yyyy-MM-dd 형식의 문자열)</li>
 *     <li><b>referenceDate</b> - 기준일 (연차를 계산할 기준 날짜)</li>
 *     <li><b>hasExcludedWorkPeriod</b> - 근무 제외 기간 존재 여부</li>
 *     <li><b>excludedWorkPeriod</b> - 근무 제외 기간 리스트 (시작일~종료일 범위 목록)</li>
 * </ul>
 *
 * 회계연도 방식(calculationType = 2)을 사용하는 경우에는,
 * {@code fiscalYearStartDate} 필드를 별도로 받아야 하며,
 * 현재 구조에서는 {@link AnnualLeaveContext}로 변환 시 해당 값의 유효성을 검증합니다.
 *
 * @author 김재윤
 * @see com.lawding.leavecalc.domain.annualleave.AnnualLeaveContext
 * @see com.lawding.leavecalc.domain.DateRange
 */

@Getter
public class AnnualLeaveRequest{
    private int caculationType;
    private String hireDate;
    private String referenceDate;
    private boolean hasExcludedWorkPeriod;
    private List<DateRange> excludedWorkPeriod;


}
