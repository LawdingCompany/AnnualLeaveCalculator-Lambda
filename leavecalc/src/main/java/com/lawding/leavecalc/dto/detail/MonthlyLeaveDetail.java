package com.lawding.leavecalc.dto.detail;

import com.lawding.leavecalc.domain.MonthlyLeaveRecord;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class MonthlyLeaveDetail implements CalculationDetail {
    // 월차
    // 총 몇 개월의 근무 인지 -> 이거는 월차 계산 방식에서 몇 개인지 가져오기
    // 그 기간 중 0 인 개수 : 구하는 건
    private final List<MonthlyLeaveRecord> records;
    private final double totalLeaveDays;

}
