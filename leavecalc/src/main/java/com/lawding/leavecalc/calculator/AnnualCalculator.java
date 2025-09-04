package com.lawding.leavecalc.calculator;

import static com.lawding.leavecalc.constant.AnnualLeaveConstants.BASE_ANNUAL_LEAVE;
import static com.lawding.leavecalc.constant.AnnualLeaveConstants.MINIMUM_WORK_RATIO;
import static com.lawding.leavecalc.util.AnnualLeaveHelper.*;

import com.lawding.leavecalc.domain.flow.context.AnnualContext;
import com.lawding.leavecalc.dto.detail.AnnualDetail;
import com.lawding.leavecalc.dto.detail.CalculationDetail;

public class AnnualCalculator implements LeaveCalculator<AnnualContext> {

    @Override
    public CalculationDetail calculate(AnnualContext context) {
        int additionalLeave = calculateAdditionalLeave(context.getServiceYears());
        double totalLeaveDays;
        if (context.getPrescribedWorkingRatio() < MINIMUM_WORK_RATIO) {
            totalLeaveDays = formatDouble(
                (BASE_ANNUAL_LEAVE + additionalLeave) * context.getPrescribedWorkingRatio());
        } else {
            totalLeaveDays = BASE_ANNUAL_LEAVE + additionalLeave;
        }
        return AnnualDetail.builder()
            .additionalLeave(additionalLeave)
            .totalLeaveDays(totalLeaveDays)
            .build();
    }
}