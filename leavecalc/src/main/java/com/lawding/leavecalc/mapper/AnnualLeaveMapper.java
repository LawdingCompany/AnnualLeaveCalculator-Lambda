package com.lawding.leavecalc.mapper;

import static com.lawding.leavecalc.util.DateParseUtils.*;

import com.lawding.leavecalc.domain.AnnualLeaveContext;
import com.lawding.leavecalc.domain.CalculationType;
import com.lawding.leavecalc.domain.DatePeriod;
import com.lawding.leavecalc.dto.request.AnnualLeaveRequest;
import com.lawding.leavecalc.dto.request.NonWorkingPeriodDto;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


public class AnnualLeaveMapper {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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
            .nonWorkingPeriods(groupByNonWorkingType(request.getNonWorkingPeriods())) // null -> map.of()
            .companyHolidays(convertToLocalDates(request.getCompanyHolidays())) // null -> map.of()
            .build();
    }

    public static Map<Integer, List<DatePeriod>> groupByNonWorkingType(
        List<NonWorkingPeriodDto> nonWorkingPeriodDtos
    ) {
        return Optional.ofNullable(nonWorkingPeriodDtos)
            .orElse(List.of()) // null이면 빈 리스트
            .stream()
            .collect(Collectors.groupingBy(
                NonWorkingPeriodDto::getType,
                Collectors.mapping(
                    period -> new DatePeriod(
                        LocalDate.parse(period.getStartDate()),
                        LocalDate.parse(period.getEndDate())
                    ),
                    Collectors.toList()
                )
            ));
    }

    public static List<NonWorkingPeriodDto> toDtoList(Map<Integer, List<DatePeriod>> nonWorkingPeriods) {
        if (nonWorkingPeriods == null || nonWorkingPeriods.isEmpty()) {
            return List.of();
        }

        return nonWorkingPeriods.entrySet().stream()
            .flatMap(entry -> entry.getValue().stream()
                .map(period -> NonWorkingPeriodDto.builder()
                    .type(entry.getKey())
                    .startDate(period.startDate().format(FORMATTER))
                    .endDate(period.endDate().format(FORMATTER))
                    .build()
                )
            )
            .toList();
    }

    public static List<String> toStringList(List<LocalDate> dates) {
        if (dates == null || dates.isEmpty()) {
            return List.of();
        }
        return dates.stream()
            .map(date -> date.format(FORMATTER)) // LocalDate → String
            .toList();
    }

}
