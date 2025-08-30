package com.lawding.leavecalc.domain.flow.hiredate;

import com.lawding.leavecalc.domain.flow.FlowResult;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@ToString
public class UnderPWRFlowResult extends FlowResult {
    // 입사일 1년 이상 & AR >= 80 & PWR < 80
    // (기본연차 + 가산연차) * PWR
    private final double attendanceRate;
    private final double prescribeWorkingRatio;

}
