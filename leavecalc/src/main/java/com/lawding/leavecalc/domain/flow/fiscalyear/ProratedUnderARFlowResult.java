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
public class ProratedUnderARFlowResult extends FlowResult {
    // 비례연차 발생 & 출근율 80% 미만
    // 월차 형태의 비례연차
    private final Set<LocalDate> absentDays;
    private final Set<LocalDate> excludedDays;
    private final Set<LocalDate> holidays;
    private final double attendanceRate;
}
