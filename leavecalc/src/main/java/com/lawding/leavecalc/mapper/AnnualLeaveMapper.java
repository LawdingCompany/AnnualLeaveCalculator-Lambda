package com.lawding.leavecalc.mapper;

import com.lawding.leavecalc.domain.AnnualLeaveContext;
import com.lawding.leavecalc.domain.CalculationType;
import com.lawding.leavecalc.dto.AnnualLeaveRequest;
import java.time.LocalDate;
import org.apache.logging.log4j.core.util.datetime.DateParser;


public class AnnualLeaveMapper {

    /**
     * AnnualLeaveRequest를 도메인 컨텍스트인 AnnualLeaveContext로 변환합니다.
     *
     * @param request HTTP 요청으로부터 받은 DTO
     * @return 계산기 내부에서 사용하는 컨텍스트 객체
     */
    public static AnnualLeaveContext toContext(AnnualLeaveRequest request) {
        return AnnualLeaveContext.builder()
            .calculationType(CalculationType.fromCode(request.getCaculationType()))
            .fiscalYear(parseNullable(request.getFiscalYear()))
            .hireDate(LocalDate.parse(request.getHireDate()))
            .referenceDate(LocalDate.parse(request.getReferenceDate()))
            .hasExcludedWorkPeriod(request.isHasExcludedWorkPeriod())
            .excludedWorkPeriod(request.getExcludedWorkPeriod())
            .build();
    }

    public static LocalDate parseNullable(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        return LocalDate.parse(dateStr);
    }

}
