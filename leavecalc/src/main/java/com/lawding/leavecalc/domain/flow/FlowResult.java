package com.lawding.leavecalc.domain.flow;

import com.lawding.leavecalc.domain.FlowStep;
import com.lawding.leavecalc.domain.DatePeriod;
import com.lawding.leavecalc.domain.LeaveType;
import com.lawding.leavecalc.domain.flow.context.CalculationContext;
import java.util.List;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@ToString
public class FlowResult {
    private final List<FlowStep> steps;
    private final LeaveType leaveType;
    private final CalculationContext context;
}
