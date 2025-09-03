package com.lawding.leavecalc.strategy;

import com.lawding.leavecalc.domain.AnnualLeaveContext;
import com.lawding.leavecalc.dto.AnnualLeaveResult;

public sealed interface CalculationStrategy permits HireDateStrategy, FiscalYearStrategy {
    AnnualLeaveResult annualLeaveCalculate(AnnualLeaveContext annualLeaveContext);
}