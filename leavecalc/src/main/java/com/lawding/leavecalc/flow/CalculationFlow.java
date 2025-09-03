package com.lawding.leavecalc.flow;

import com.lawding.leavecalc.domain.AnnualLeaveContext;
import com.lawding.leavecalc.domain.flow.FlowResult;
import java.time.LocalDate;

public interface CalculationFlow {
    // 계산 흐름
    public FlowResult process(AnnualLeaveContext context);

    // 근속 연차 계산
    public int calculateServiceYears(LocalDate startDate, LocalDate endDate);
}

