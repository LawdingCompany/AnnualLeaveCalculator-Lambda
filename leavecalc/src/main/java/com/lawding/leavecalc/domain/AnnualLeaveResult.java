package com.lawding.leavecalc.domain;


import com.lawding.leavecalc.domain.detail.CalculationDetail;
import java.time.LocalDate;
import java.time.MonthDay;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class AnnualLeaveResult {

    private final CalculationType calculationType;
    private final AnnualLeaveResultType annualLeaveResultType;
    private final LocalDate hireDate;
    private final MonthDay fiscalYear;
    private final LocalDate referenceDate;
    private final CalculationDetail calculationDetail;
    private final String explanation;

}
