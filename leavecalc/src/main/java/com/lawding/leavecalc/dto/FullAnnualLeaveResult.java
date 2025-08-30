package com.lawding.leavecalc.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class FullAnnualLeaveResult {
    private final int additionalLeave;  // 가산연차
    private final int totalLeaveDays;  // 기본 + 가산 연차
}
