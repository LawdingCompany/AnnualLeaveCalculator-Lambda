package com.lawding.leavecalc.mapper;

import static com.lawding.leavecalc.util.DateParseUtils.*;

import com.lawding.leavecalc.domain.AnnualLeaveContext;
import com.lawding.leavecalc.domain.CalculationType;
import com.lawding.leavecalc.domain.DatePeriod;
import com.lawding.leavecalc.dto.AnnualLeaveRequest;
import com.lawding.leavecalc.dto.NonWorkingPeriodRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


public class AnnualLeaveMapper {

    private AnnualLeaveMapper() {
    }

    /**
     * AnnualLeaveRequest를 도메인 컨텍스트인 AnnualLeaveContext로 변환합니다.
     *
     * @param request HTTP 요청으로부터 받은 DTO
     * @return 계산기 내부에서 사용하는 컨텍스트 객체
     */
    public static AnnualLeaveContext toContext(AnnualLeaveRequest request) {
        return AnnualLeaveContext.builder()
            .calculationType(CalculationType.fromCode(request.getCalculationType()))
            .fiscalYear(parseNullableMonthDay(request.getFiscalYear()))
            .hireDate(LocalDate.parse(request.getHireDate()))
            .referenceDate(LocalDate.parse(request.getReferenceDate()))
            .nonWorkingPeriods(groupByNonWorkingType(request.getNonWorkingPeriods()))
            .companyHolidays(convertToLocalDates(request.getCompanyHolidays()))
            .build();
    }

    public static Map<Integer, List<DatePeriod>> groupByNonWorkingType(
        List<NonWorkingPeriodRequest> nonWorkingPeriodRequests
    ) {
        return Optional.ofNullable(nonWorkingPeriodRequests)
            .orElse(List.of()) // null이면 빈 리스트
            .stream()
            .collect(Collectors.groupingBy(
                NonWorkingPeriodRequest::type,
                Collectors.mapping(
                    period -> new DatePeriod(
                        LocalDate.parse(period.startDate()),
                        LocalDate.parse(period.endDate())
                    ),
                    Collectors.toList()
                )
            ));
    }


}
