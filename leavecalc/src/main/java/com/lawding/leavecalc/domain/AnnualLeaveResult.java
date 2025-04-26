package com.lawding.leavecalc.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AnnualLeaveResult {

    private double annualLeaveDays;
    private String explanation;

}
