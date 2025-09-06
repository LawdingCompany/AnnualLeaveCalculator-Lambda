package com.lawding.leavecalc.domain.flow.detail;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@ToString
public class ProratedDetail extends CalculationDetail {
    private final double prescribedWorkingRatioForProrated;
}
