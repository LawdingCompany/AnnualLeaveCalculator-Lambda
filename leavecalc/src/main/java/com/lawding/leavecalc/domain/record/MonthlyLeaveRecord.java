package com.lawding.leavecalc.domain.record;

import com.lawding.leavecalc.domain.DatePeriod;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class MonthlyLeaveRecord {
    private final DatePeriod period;
    private final double monthlyLeave;
}
