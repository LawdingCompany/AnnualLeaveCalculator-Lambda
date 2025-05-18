package com.lawding.leavecalc.domain;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * 연차 계산에 필요한 정보를 담은 객체입니다.
 * <p>
 *
 * calculationType   연차 산정 방식 (1:입사일, 2:회계연도)
 * fiscalYear        회계연도(nullable)
 * hireDate          입사일
 * referenceDate     기준일(연차 산정 기준일)
 * nonWorkingPeriods 비근무 기간
 * companyHolidays   회사 공휴일
 */
@Getter
@Builder
@ToString
public class AnnualLeaveContext {
    private CalculationType calculationType;
    private MonthDay fiscalYear;
    private LocalDate hireDate;
    private LocalDate referenceDate;
    private Map<Integer, List<DatePeriod>> nonWorkingPeriods;
    private List<LocalDate> companyHolidays;

}
