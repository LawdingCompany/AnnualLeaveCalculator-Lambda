package com.lawding.leavecalc.calculator;

import static com.lawding.leavecalc.constant.AnnualLeaveConstants.BASE_ANNUAL_LEAVE;
import static com.lawding.leavecalc.util.AnnualLeaveHelper.formatDouble;

import com.lawding.leavecalc.domain.flow.context.ProratedContext;
import com.lawding.leavecalc.dto.detail.CalculationDetail;
import com.lawding.leavecalc.dto.detail.ProratedDetail;

public class ProratedCalculator implements LeaveCalculator<ProratedContext> {

    @Override
    public CalculationDetail calculate(ProratedContext context) {
        double prescribeWorkingRatio = context.getPrescribeWorkingRatio();
        double proratedLeaveDays =
            formatDouble(BASE_ANNUAL_LEAVE * prescribeWorkingRatio);

        return ProratedDetail.builder()
            .totalLeaveDays(proratedLeaveDays)
            .build();
    }
}