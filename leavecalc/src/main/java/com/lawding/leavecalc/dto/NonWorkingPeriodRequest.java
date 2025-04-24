package com.lawding.leavecalc.dto;

public record NonWorkingPeriodRequest(
    int type,
    String startDate,
    String endDate
) {

}
