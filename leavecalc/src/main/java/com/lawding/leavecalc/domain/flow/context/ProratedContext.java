package com.lawding.leavecalc.domain.flow.context;

import com.lawding.leavecalc.domain.DatePeriod;
import java.time.LocalDate;
import java.util.Set;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@ToString
public class ProratedContext extends CalculationContext{
    private final double prescribedWorkingRatioForProrated;
}
