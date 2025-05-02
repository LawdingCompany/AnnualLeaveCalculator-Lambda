package com.lawding.leavecalc.domain.result;

public enum AnnualLeaveResultType {
    MONTHLY,    // 월차
    FULL, // 기본 + 가산
    ADJUSTED, // (기본 + 가산) PWR
    PRORATED, // 15 * PWR
    MONTHY_PRORATED // 월차 + 15 * PWR

}
