package com.lawding.leavecalc.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class AdjustedAnnualLeaveResult {
    private final int additionalLeave;  // 가산연차
    private final double adjustedLeaveDays; // 비례삭감연차
}
