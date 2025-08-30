package com.lawding.leavecalc.domain.flow.fiscalyear;

import com.lawding.leavecalc.domain.flow.FlowResult;
import com.lawding.leavecalc.domain.flow.MonthlyCalcContext;
import java.time.LocalDate;
import java.util.Set;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@ToString
public class BeforeProratedFlowResult extends FlowResult implements MonthlyCalcContext {
    private final Set<LocalDate> absentDays;
    private final Set<LocalDate> excludedDays;
    private final Set<LocalDate> holidays;
}
