package com.lawding.leavecalc.dto.detail;

import static com.lawding.leavecalc.constant.AnnualLeaveConstants.BASE_ANNUAL_LEAVE;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@ToString
public class AnnualDetail extends CalculationDetail {

    private final int baseAnnualLeave = BASE_ANNUAL_LEAVE;
    private final int additionalLeave;
}
