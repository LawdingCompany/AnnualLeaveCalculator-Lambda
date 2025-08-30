package com.lawding.leavecalc.domain.flow.fiscalyear;

import com.lawding.leavecalc.domain.flow.FlowResult;
import java.time.LocalDate;
import java.util.Set;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@ToString
public class MonthlyAndProratedFlowResult extends FlowResult {

    private final BeforeProratedFlowResult monthlyPart;
    private final FlowResult proratedPart;
}
