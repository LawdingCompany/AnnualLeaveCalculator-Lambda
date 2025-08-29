package com.lawding.leavecalc.domain.flow;

import com.lawding.leavecalc.domain.Condition;
import com.lawding.leavecalc.domain.DatePeriod;
import com.lawding.leavecalc.domain.LeaveType;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@ToString
public abstract class FlowResult {
    private final LeaveType leaveType;
    private final DatePeriod accrualPeriod;
    private final Condition condition;
}
