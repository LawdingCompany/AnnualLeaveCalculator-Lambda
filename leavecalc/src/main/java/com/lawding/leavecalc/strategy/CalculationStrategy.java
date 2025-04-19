package com.lawding.leavecalc.strategy;

import com.lawding.leavecalc.domain.AnnualLeaveResult;

public sealed interface CalculationStrategy permits HireDateStrategy,FiscalYearStrategy{
    AnnualLeaveResult annualLeaveCalculate();
}