package com.lawding.leavecalc.domain.flow.hiredate;

import com.lawding.leavecalc.domain.flow.FlowResult;
import com.lawding.leavecalc.domain.flow.MonthlyCalcContext;
import java.time.LocalDate;
import java.util.Set;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@ToString
public class LowARFlowResult extends FlowResult implements MonthlyCalcContext {
    // 입사일 1년 이상 & 출근율 80% 미만
    // 월차
    private final Set<LocalDate> absentDays;
    private final Set<LocalDate> excludedDays;
    private final double attendanceRate;
}
