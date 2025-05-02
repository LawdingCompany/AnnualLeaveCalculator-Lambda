package com.lawding.leavecalc.domain.detail;

import com.lawding.leavecalc.domain.DatePeriod;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MonthlyLeaveDetail implements CalculationDetail {
    // 월차
    private final List<MonthlyLeaveGrantRecord> records;
    private final double totalLeaveDays;

    @Builder
    public static class MonthlyLeaveGrantRecord {

        DatePeriod period;
        double monthlyLeave;
    }
}
