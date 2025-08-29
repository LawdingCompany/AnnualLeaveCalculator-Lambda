package com.lawding.leavecalc.domain.flow.hiredate;

import com.lawding.leavecalc.domain.condition.HireDateCondition;
import com.lawding.leavecalc.domain.flow.FlowResult;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@ToString
public class LessOneYearFlowResult extends FlowResult {
    private final HireDateCondition condition;
}
