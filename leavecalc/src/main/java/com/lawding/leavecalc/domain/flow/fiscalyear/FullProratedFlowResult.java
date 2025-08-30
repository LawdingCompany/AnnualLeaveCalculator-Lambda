package com.lawding.leavecalc.domain.flow.fiscalyear;

import com.lawding.leavecalc.domain.flow.FlowResult;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@ToString
public class FullProratedFlowResult extends FlowResult {
    private final double prescribeWorkingRatio;
    private final double attendanceRate;
}
