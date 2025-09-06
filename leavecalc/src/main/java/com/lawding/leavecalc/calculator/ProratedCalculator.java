package com.lawding.leavecalc.calculator;

import static com.lawding.leavecalc.constant.AnnualLeaveConstants.BASE_ANNUAL_LEAVE;
import static com.lawding.leavecalc.util.AnnualLeaveHelper.formatDouble;

import com.lawding.leavecalc.domain.flow.context.ProratedContext;
import com.lawding.leavecalc.domain.flow.detail.CalculationDetail;
import com.lawding.leavecalc.domain.flow.detail.ProratedDetail;

public class ProratedCalculator implements LeaveCalculator<ProratedContext> {

    @Override
    public CalculationDetail calculate(ProratedContext context) {
        double prescribeWorkingRatio = context.getPrescribedWorkingRatioForProrated();
        double proratedLeaveDays =
            formatDouble(BASE_ANNUAL_LEAVE * prescribeWorkingRatio);

        return ProratedDetail.builder()
            .accrualPeriod(context.getAccrualPeriod())
            .availablePeriod(context.getAvailablePeriod())
            .attendanceRate(context.getAttendanceRate())
            .prescribedWorkingRatio(context.getPrescribedWorkingRatio())
            .prescribedWorkingRatioForProrated(formatDouble(context.getPrescribedWorkingRatioForProrated()))
            .serviceYears(context.getServiceYears())
            .totalLeaveDays(proratedLeaveDays)
            .build();
    }
}