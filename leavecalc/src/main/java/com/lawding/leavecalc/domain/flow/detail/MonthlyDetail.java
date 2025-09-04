package com.lawding.leavecalc.domain.flow.detail;

import com.lawding.leavecalc.domain.MonthlyLeaveRecord;
import java.util.List;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@ToString
public class MonthlyDetail extends CalculationDetail{
    private final List<MonthlyLeaveRecord> records;
}
