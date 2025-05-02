package com.lawding.leavecalc.domain;


import com.lawding.leavecalc.domain.detail.CalculationDetail;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AnnualLeaveResult {

    private final AnnualLeaveResultType type;
    private final LocalDate hireDate;
    private final LocalDate referenceDate;
    private final CalculationDetail calculationDetail;
    private final String explanation;

}
