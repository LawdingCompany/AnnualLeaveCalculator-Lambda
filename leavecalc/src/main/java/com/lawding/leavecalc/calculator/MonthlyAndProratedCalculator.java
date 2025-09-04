package com.lawding.leavecalc.calculator;

import com.lawding.leavecalc.domain.flow.context.CalculationContext;
import com.lawding.leavecalc.domain.flow.context.MonthlyAndProratedContext;
import com.lawding.leavecalc.domain.flow.context.MonthlyContext;
import com.lawding.leavecalc.domain.flow.context.ProratedContext;
import com.lawding.leavecalc.domain.flow.detail.CalculationDetail;
import com.lawding.leavecalc.domain.flow.detail.MonthlyAndProratedDetail;
import com.lawding.leavecalc.domain.flow.detail.MonthlyDetail;
import com.lawding.leavecalc.exception.AnnualLeaveException;
import com.lawding.leavecalc.exception.ErrorCode;

public class MonthlyAndProratedCalculator implements LeaveCalculator<MonthlyAndProratedContext> {

    private final MonthlyCalculator monthlyCalc;
    private final ProratedCalculator proratedCalc;

    public MonthlyAndProratedCalculator(MonthlyCalculator monthlyCalc,
        ProratedCalculator proratedCalc) {
        this.monthlyCalc = monthlyCalc;
        this.proratedCalc = proratedCalc;
    }

    @Override
    public CalculationDetail calculate(MonthlyAndProratedContext context) {
        MonthlyContext monthlyContext = context.getMonthlyContext();
        MonthlyDetail monthlyDetail = (MonthlyDetail) monthlyCalc.calculate(monthlyContext);

        CalculationContext proratedContext = context.getProratedContext();
        CalculationDetail proratedDetail;
        if (proratedContext instanceof MonthlyContext mCtx) {
            proratedDetail = monthlyCalc.calculate(mCtx);
        } else if (proratedContext instanceof ProratedContext pCtx) {
            proratedDetail = proratedCalc.calculate(pCtx);
        } else {
            throw new AnnualLeaveException(ErrorCode.PRORATED_TYPE_ERROR);
        }
        double totalLeaveDays =
            monthlyDetail.getTotalLeaveDays() + proratedDetail.getTotalLeaveDays();
        return MonthlyAndProratedDetail
            .builder()
            .totalLeaveDays(totalLeaveDays)
            .monthlyDetail(monthlyDetail)
            .proratedDetail(proratedDetail)
            .build();
    }
}