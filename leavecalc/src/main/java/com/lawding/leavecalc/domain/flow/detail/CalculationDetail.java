package com.lawding.leavecalc.domain.flow.detail;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@SuperBuilder
@ToString
public abstract class CalculationDetail {
    private final double totalLeaveDays;
    private final Double attendanceRate;
    private final Double prescribedWorkingRatio;
}
