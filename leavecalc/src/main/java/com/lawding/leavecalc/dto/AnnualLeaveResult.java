package com.lawding.leavecalc.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.lawding.leavecalc.domain.flow.detail.CalculationDetail;
import com.lawding.leavecalc.dto.request.NonWorkingPeriodDto;
import java.util.List;
import lombok.Builder;

@Builder
public record AnnualLeaveResult(
    String calculationType, // 산정 방식 : 입사일 | 회계연도
    String fiscalYear,  // 회계연도 시작일 (nullable)
    String hireDate,    // 입사일
    String referenceDate,   // 산정 기준일
    List<NonWorkingPeriodDto> nonWorkingPeriod, // 특이사항 기간 (nullable)
    List<String> companyHolidays,   // 회사자체휴일(nullable)
    
    String leaveType, // 연차 형태
    CalculationDetail calculationDetail,    // 계산 결과
    List<String> explanations,   // 설명
    List<String> nonWorkingExplanations // 특이사항 안내
) {

}
